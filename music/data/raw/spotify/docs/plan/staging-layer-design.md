# Staging Layer Design

Back to [main plan](SPOTIFY_IMPLEMENTATION_PLAN.md#4-staging-layer-design)

## Overview

The staging layer sits between response parsing and target table population. Every parsed API response writes to staging tables, never directly to target tables. A separate Staging Applicator service periodically applies staged data to target tables in controlled batches.

## Hot/Cold Table Swap

### Mechanism

For each target table, two staging tables exist: `stg_{table}_a` and `stg_{table}_b`. At any given time:
- One table is the **write target** (the Parser writes here)
- The other is the **read source** (the Applicator reads from here)

A metadata table `staging_control` tracks which table is active for writing:

```sql
CREATE TABLE staging_control (
    target_table    VARCHAR(64) PRIMARY KEY,
    write_target    CHAR(1) NOT NULL DEFAULT 'A',  -- 'A' or 'B'
    last_swap_at    TIMESTAMPTZ,
    last_apply_at   TIMESTAMPTZ,
    records_applied BIGINT DEFAULT 0
);
```

### Swap Protocol

```
1. Applicator acquires advisory lock (pg_advisory_lock) for the target table
2. Applicator reads staging_control to determine current write_target (e.g., 'A')
3. Applicator flips write_target to 'B' (UPDATE staging_control SET write_target = 'B')
4. From this moment, Parser writes to table B
5. Applicator processes all records from table A
6. After successful application, Applicator TRUNCATEs table A
7. Applicator releases advisory lock
```

The advisory lock prevents two Applicator instances from swapping simultaneously. The Parser doesn't need the lock — it simply reads `staging_control.write_target` before each batch write.

### Why This Works

- **No contention**: Parser and Applicator never touch the same staging table simultaneously
- **No lost writes**: The swap is a single UPDATE; Parser reads the target atomically before each write
- **Crash safety**: If the Applicator crashes mid-apply, table A still has all records. On restart, it re-processes A (idempotent application via UPSERT)
- **Truncate is fast**: TRUNCATE is O(1) vs DELETE which scans all rows

### Staging Tables Per Target

| Target Table | Staging A | Staging B |
|-------------|-----------|-----------|
| `artist` | `stg_artist_a` | `stg_artist_b` |
| `album` | `stg_album_a` | `stg_album_b` |
| `track` | `stg_track_a` | `stg_track_b` |
| `entity_relation` | `stg_entity_relation_a` | `stg_entity_relation_b` |
| `attribute_history` | `stg_attribute_history_a` | `stg_attribute_history_b` |

Total: 10 staging tables + 1 control table.

## Staging Table Schema

Each staging table mirrors its target table columns, plus staging metadata:

```sql
CREATE TABLE stg_artist_a (
    -- Staging metadata
    id              BIGSERIAL PRIMARY KEY,
    batch_id        BIGINT NOT NULL,           -- Groups records from same parsing cycle
    api_response_id BIGINT NOT NULL,           -- Provenance: which response produced this
    staged_at       TIMESTAMPTZ DEFAULT now(),
    status          SMALLINT DEFAULT 0,        -- 0=PENDING, 1=APPLIED, 2=FAILED, 3=SKIPPED

    -- Synthetic or real ID
    entity_id       BIGINT,                    -- Synthetic ID (negative) or real ID (positive)
    spotify_id      VARCHAR(64) NOT NULL,      -- Spotify's canonical ID

    -- Target table columns (mirror artist table)
    name            VARCHAR(1024),
    genres          TEXT,                       -- JSON array
    images          TEXT,                       -- JSON array
    external_urls   TEXT,                       -- JSON object
    uri             VARCHAR(256),

    -- Deduplication
    UNIQUE (spotify_id)                        -- Only latest staging record per spotify_id
);
```

The `UNIQUE (spotify_id)` constraint with an upsert (ON CONFLICT DO UPDATE) ensures that if the same Spotify entity appears in multiple API responses within the same staging window, only the latest values are kept. This mirrors LastFM's staging dedup approach.

## Application Process

The Applicator processes each target table independently in this order:

```
1. Apply artists    (entities first — they have no FK dependencies)
2. Apply albums     (depends on artists for primary_artist_id)
3. Apply tracks     (depends on albums)
4. Apply entity_relation  (depends on all entity types)
5. Apply attribute_history (depends on all entity types)
```

### Application SQL Pattern (Artist Example)

```sql
-- Step 1: Resolve synthetic IDs for records referencing existing entities
WITH resolve_existing AS (
    SELECT s.id as staging_id, a.id as real_id
    FROM stg_artist_a s
    JOIN artist a ON a.spotify_id = s.spotify_id
    WHERE s.status = 0  -- PENDING
),

-- Step 2: Insert new entities (no matching spotify_id in target)
inserted AS (
    INSERT INTO artist (id, spotify_id, name, genres, images, external_urls, uri,
                        api_response_id, created_dttm, modified_dttm)
    SELECT nextval('artist_seq'), s.spotify_id, s.name, s.genres, s.images,
           s.external_urls, s.uri, s.api_response_id, now(), now()
    FROM stg_artist_a s
    LEFT JOIN resolve_existing re ON re.staging_id = s.id
    WHERE s.status = 0
      AND re.staging_id IS NULL  -- Not existing
    ON CONFLICT (spotify_id) DO NOTHING  -- Race condition safety
    RETURNING id, spotify_id
),

-- Step 3: Update existing entities where data changed
updated AS (
    UPDATE artist a
    SET name = COALESCE(s.name, a.name),
        genres = COALESCE(s.genres, a.genres),
        images = COALESCE(s.images, a.images),
        external_urls = COALESCE(s.external_urls, a.external_urls),
        uri = COALESCE(s.uri, a.uri),
        api_response_id = s.api_response_id,
        modified_dttm = now()
    FROM stg_artist_a s
    JOIN resolve_existing re ON re.staging_id = s.id
    WHERE a.id = re.real_id
      AND s.status = 0
      AND (a.name IS DISTINCT FROM s.name
           OR a.genres IS DISTINCT FROM s.genres
           OR a.images IS DISTINCT FROM s.images)
    RETURNING a.id, a.spotify_id
),

-- Step 4: Build synthetic→real ID mapping for downstream tables
id_mapping AS (
    SELECT spotify_id, id as real_id FROM inserted
    UNION ALL
    SELECT spotify_id, real_id FROM resolve_existing
)

-- Step 5: Mark staging records as applied
UPDATE stg_artist_a SET status = 1 WHERE status = 0;
```

The `id_mapping` CTE result is persisted to a `synthetic_id_resolution` table so that subsequent target table applications (albums, tracks, relations) can resolve their foreign key references.

## Comparison with LastFM Staging

| Aspect | LastFM | Spotify |
|--------|--------|---------|
| **Scope** | Attribute history only | All entity types |
| **Tables** | 2 (stg_a, stg_b for attribute_history) | 10 (2 per target table) |
| **Dedup in staging** | UPSERT on composite key | UPSERT on spotify_id |
| **Application** | CTE pipeline (archive + delete + insert) | CTE pipeline (resolve + insert + update) |
| **ID resolution** | Not needed (entities exist before staging) | Required (synthetic → real ID mapping) |
| **Error tracking** | None (all-or-nothing per batch) | Per-record status column |
| **Control mechanism** | Alternating table name in code | `staging_control` metadata table |

## Failure Handling

### Parser Crash Mid-Write
- Staging records written before crash are committed (per-response transactions)
- Unwritten records simply don't appear in staging — the API responses remain PENDING and will be re-processed

### Applicator Crash Mid-Apply
- Advisory lock is released on connection close
- On restart, Applicator finds unapplied records (status=PENDING) in the same table
- Application is idempotent (UPSERT + ON CONFLICT DO NOTHING)

### Swap Failure
- If swap UPDATE fails, Applicator retries on next cycle
- Parser continues writing to the old target (no harm — just delays application)
