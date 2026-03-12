# Staging Layer Design

Back to [main plan](SPOTIFY_IMPLEMENTATION_PLAN.md#4-staging-layer-design)

## Overview

The staging layer sits between response parsing and target table population. Every parsed API response writes to staging tables, never directly to target tables. A separate Staging Applicator service applies staged data to target tables in controlled batches.

## Iteration-Based Staging

### Why Not A/B Tables

The initial A/B (hot/cold) swap design has a critical failure mode: if processing of table A fails, its records stay. The next swap makes B the read source — but A still has unprocessed data. Over time, failed records accumulate, tables grow, and the system degrades. Retrying the failed table risks conflicting with the live swap cycle.

### The Iteration Model

Instead of two fixed tables, we use **numbered staging iterations**. Each iteration is a self-contained batch with its own set of staging tables:

```
stg_artist_00042, stg_album_00042, stg_track_00042, ...   ← iteration 42
stg_artist_00043, stg_album_00043, stg_track_00043, ...   ← iteration 43
stg_artist_00044, stg_album_00044, stg_track_00044, ...   ← iteration 44 (currently being written)
```

A metadata table tracks iteration lifecycle:

```sql
CREATE TABLE staging_iteration (
    id              BIGINT PRIMARY KEY DEFAULT nextval('staging_iteration_seq'),
    status          SMALLINT NOT NULL DEFAULT 0,
    -- 0=OPEN (parser is writing), 1=SEALED (ready for application),
    -- 2=APPLYING, 3=COMPLETED, 4=FAILED, 5=RETRYING

    records_staged  BIGINT DEFAULT 0,
    records_applied BIGINT DEFAULT 0,
    records_failed  BIGINT DEFAULT 0,

    opened_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    sealed_at       TIMESTAMPTZ,
    applied_at      TIMESTAMPTZ,
    error_message   TEXT,

    -- For retry tracking
    retry_count     SMALLINT DEFAULT 0,
    last_retry_at   TIMESTAMPTZ
);

CREATE SEQUENCE staging_iteration_seq INCREMENT BY 1;
```

### Iteration Lifecycle

```
                 ┌─────────┐
                 │  OPEN   │ ← Parser writes to this iteration's tables
                 └────┬────┘
                      │ seal (time window or record count threshold)
                      ▼
                 ┌─────────┐
                 │ SEALED  │ ← Ready for application
                 └────┬────┘
                      │ applicator picks up
                      ▼
                 ┌──────────┐
                 │ APPLYING │
                 └────┬─────┘
                    ╱     ╲
                   ╱       ╲
                  ▼         ▼
          ┌───────────┐  ┌────────┐
          │ COMPLETED │  │ FAILED │
          └───────────┘  └───┬────┘
                             │ retry job picks up
                             ▼
                        ┌──────────┐
                        │ RETRYING │
                        └────┬─────┘
                           ╱     ╲
                          ▼       ▼
                    COMPLETED   FAILED (retry_count++)
```

### Seal Criteria

An OPEN iteration is sealed (transitions to SEALED) when any of these conditions are met:
- **Time window**: iteration has been open for > N minutes (configurable, e.g., 5 min)
- **Record count**: total staged records across all tables > M (configurable, e.g., 5000)
- **Manual trigger**: via REST API for testing/debugging

A scheduled job checks seal criteria every 30 seconds. When it seals the current iteration, it immediately opens a new one (creates tables, inserts metadata row) so the Parser always has a write target.

### Table Creation

When a new iteration is opened, its staging tables are created from templates:

```sql
-- Template table (never written to directly, serves as DDL source)
CREATE TABLE stg_artist_template (
    id              BIGSERIAL PRIMARY KEY,
    api_response_id BIGINT NOT NULL,
    staged_at       TIMESTAMPTZ DEFAULT now(),

    -- Synthetic or real ID
    entity_id       BIGINT,
    spotify_id      VARCHAR(64) NOT NULL,

    -- Mirror of target columns
    name            VARCHAR(1024),
    spotify_url     VARCHAR(512),
    uri             VARCHAR(256),

    -- Per-iteration dedup: last writer wins
    UNIQUE (spotify_id)
);
-- Analogous templates for genre, album, track, entity_relation, attribute_history
-- See database-schema.md for full template definitions

-- When opening iteration 44:
CREATE TABLE stg_artist_00044 (LIKE stg_artist_template INCLUDING ALL);
CREATE TABLE stg_genre_00044 (LIKE stg_genre_template INCLUDING ALL);
CREATE TABLE stg_album_00044 (LIKE stg_album_template INCLUDING ALL);
CREATE TABLE stg_track_00044 (LIKE stg_track_template INCLUDING ALL);
CREATE TABLE stg_entity_relation_00044 (LIKE stg_entity_relation_template INCLUDING ALL);
CREATE TABLE stg_attribute_history_00044 (LIKE stg_attribute_history_template INCLUDING ALL);
```

`LIKE ... INCLUDING ALL` copies columns, constraints, indexes, and defaults. Fast DDL operation.

### Parser Writes

The Parser reads the current OPEN iteration ID from `staging_iteration` and writes to its tables:

```java
public class StagingWriter {

    private volatile long currentIterationId;

    @Scheduled(fixedDelay = 10_000)
    void refreshCurrentIteration() {
        currentIterationId = jdbcTemplate.queryForObject(
            "SELECT id FROM staging_iteration WHERE status = 0 ORDER BY id DESC LIMIT 1",
            Long.class
        );
    }

    public void stageArtist(SpotifyArtistDto dto, long apiResponseId) {
        String table = "stg_artist_" + String.format("%05d", currentIterationId);
        jdbcTemplate.update(
            "INSERT INTO " + table + " (...) VALUES (?, ?, ...) " +
            "ON CONFLICT (spotify_id) DO UPDATE SET name = EXCLUDED.name, ...",
            dto.getSpotifyId(), dto.getName(), ...
        );
    }
}
```

**Contention**: Multiple parser instances writing to the same iteration's tables will only contend on the `UNIQUE(spotify_id)` constraint if two parsers process responses containing the same entity simultaneously. The `ON CONFLICT DO UPDATE` handles this gracefully — last writer wins, no errors.

## Application Process

### Normal Application

The Applicator picks up the oldest SEALED iteration and processes it:

```
1. UPDATE staging_iteration SET status = 2 (APPLYING) WHERE id = <iter_id>
2. Apply entities in dependency order:
   a. Artists  (no FK deps)
   b. Genres   (no FK deps)
   c. Albums   (depends on artists for primary_artist_id)
   d. Tracks   (depends on albums, artists)
   e. Entity relations (depends on all entity types including genres)
   f. Attribute history (depends on all entity types)
3. On success:
   UPDATE staging_iteration SET status = 3 (COMPLETED), applied_at = now()
4. On failure:
   UPDATE staging_iteration SET status = 4 (FAILED), error_message = <msg>
```

### Retry of Failed Iterations

When no SEALED iterations are pending, the Applicator switches to retry mode:

```
1. Find oldest FAILED iteration with retry_count < max_retries (configurable, e.g., 3)
2. UPDATE staging_iteration SET status = 5 (RETRYING), retry_count = retry_count + 1
3. Apply using the SAME logic as normal application (upsert-based, inherently idempotent)
4. On success: status = 3 (COMPLETED)
5. On failure: status = 4 (FAILED), last_retry_at = now()
```

**Safety of retrying old iterations**: Since newer iterations may have already been applied, a retried old iteration could contain stale data. The application SQL handles this with timestamp guards:

```sql
-- Only update target entity if staging data is not older than what's already there
UPDATE artist a
SET name = COALESCE(s.name, a.name),
    genres = COALESCE(s.genres, a.genres),
    ...
    modified_dttm = now()
FROM stg_artist_00041 s
WHERE a.spotify_id = s.spotify_id
  AND s.staged_at > a.modified_dttm;  -- Timestamp guard: only apply if newer
```

For new entities (not yet in target table), the INSERT always succeeds regardless of iteration age — the entity needs to exist. For attribute_history, the existing SCD2 logic already has `valid_from` ordering that prevents older values from overwriting newer ones.

### The Partial-Failure Problem

The user raised an important scenario: what if iteration 41 fails after inserting artist A but before inserting A's tracks T1 and T2? Then iteration 42 succeeds with different data. When retrying iteration 41:

- **Artist A**: Already exists (from 41's partial apply or from 42). The UPSERT with timestamp guard skips it if 42's data is newer, or updates it if 41's data is somehow newer (unlikely but harmless).
- **Tracks T1, T2**: May or may not exist. If they don't exist, they're inserted. If they do (from a later iteration), timestamp guard applies.
- **Relations (A→T1, A→T2)**: UPSERT on composite key — if relation already exists, it's a no-op or update.

The key insight: **application logic is purely additive (UPSERT-based)**. It never deletes, never requires all-or-nothing semantics within a batch. Partial application of a batch is safe because:
1. Every entity has a canonical key (spotify_id) — duplicates are impossible
2. Every relation has a composite unique key — duplicates are impossible
3. Timestamp guards prevent old data from overwriting new data
4. The only risk is "orphaned" staging data that references entities not yet in the system — these are simply skipped (ON CONFLICT DO NOTHING on FK violations) and may be resolved by a later retry or a later iteration

### Application SQL Pattern (Artist Example)

```sql
WITH staging AS (
    SELECT * FROM stg_artist_00042
),

-- Insert entities not yet in the target table
inserted AS (
    INSERT INTO artist (id, spotify_id, name, genres, images, external_urls, uri,
                        api_response_id, created_dttm, modified_dttm)
    SELECT nextval('artist_seq'), s.spotify_id, s.name, s.genres, s.images,
           s.external_urls, s.uri, s.api_response_id, now(), now()
    FROM staging s
    WHERE NOT EXISTS (SELECT 1 FROM artist a WHERE a.spotify_id = s.spotify_id)
    ON CONFLICT (spotify_id) DO NOTHING
    RETURNING id, spotify_id
),

-- Update existing entities only if staging data is newer
updated AS (
    UPDATE artist a
    SET name = COALESCE(s.name, a.name),
        genres = COALESCE(s.genres, a.genres),
        images = COALESCE(s.images, a.images),
        external_urls = COALESCE(s.external_urls, a.external_urls),
        uri = COALESCE(s.uri, a.uri),
        api_response_id = s.api_response_id,
        modified_dttm = now()
    FROM staging s
    WHERE a.spotify_id = s.spotify_id
      AND s.staged_at > a.modified_dttm
      AND (a.name IS DISTINCT FROM s.name
           OR a.genres IS DISTINCT FROM s.genres
           OR a.images IS DISTINCT FROM s.images)
    RETURNING a.id, a.spotify_id
),

-- Collect all resolved IDs for downstream dependency resolution
resolved_ids AS (
    SELECT id, spotify_id FROM inserted
    UNION ALL
    SELECT a.id, a.spotify_id FROM artist a
    JOIN staging s ON s.spotify_id = a.spotify_id
    WHERE a.spotify_id NOT IN (SELECT spotify_id FROM inserted)
)

-- Persist ID mappings for subsequent table applications within this iteration
INSERT INTO synthetic_id_resolution (entity_type, spotify_id, synthetic_id, real_id)
SELECT 1, r.spotify_id, s.entity_id, r.id
FROM resolved_ids r
JOIN staging s ON s.spotify_id = r.spotify_id
WHERE s.entity_id < 0  -- Only for synthetic IDs
ON CONFLICT (entity_type, spotify_id) DO UPDATE SET real_id = EXCLUDED.real_id;
```

## Cleanup

A scheduled cleanup job runs periodically (e.g., daily) and drops staging tables for old iterations:

```sql
-- Find iterations eligible for cleanup
SELECT id FROM staging_iteration
WHERE status IN (3, 4)  -- COMPLETED or FAILED (exhausted retries)
  AND COALESCE(applied_at, opened_at) < now() - INTERVAL '7 days';
```

For each eligible iteration:
```sql
DROP TABLE IF EXISTS stg_artist_00035;
DROP TABLE IF EXISTS stg_album_00035;
DROP TABLE IF EXISTS stg_track_00035;
DROP TABLE IF EXISTS stg_entity_relation_00035;
DROP TABLE IF EXISTS stg_attribute_history_00035;
DELETE FROM staging_iteration WHERE id = 35;
```

Retention period is configurable (default: 7 days for COMPLETED, 30 days for FAILED — longer retention for failed iterations allows debugging).

## Comparison with LastFM Staging

| Aspect | LastFM | Spotify (Iteration Model) |
|--------|--------|---------------------------|
| **Scope** | Attribute history only | All entity types |
| **Table lifecycle** | 2 fixed tables, never dropped | Dynamic tables per iteration, dropped after retention |
| **Failure handling** | Retry same table (risk of accumulation) | Failed iterations retried independently, don't block new ones |
| **Isolation** | Writer and reader on different fixed tables | Writer and reader on different iterations entirely |
| **Dedup in staging** | UPSERT on composite key | UPSERT on spotify_id within iteration |
| **Application** | CTE pipeline (archive + delete + insert) | CTE pipeline (insert + conditional update + ID resolution) |
| **Timestamp safety** | `valid_from > old_valid_from` | `staged_at > modified_dttm` on entities, `valid_from` on attributes |
| **Audit trail** | Lost on truncate | Preserved for retention period |

## DDL Management Considerations

Creating tables dynamically from application code is unusual. Trade-offs:

**Pros of dynamic tables**:
- Complete isolation between iterations (no shared indexes, no row-level contention)
- DROP TABLE is instant (vs DELETE which scans and generates WAL)
- Failed iteration tables are preserved intact for debugging
- No index bloat from accumulating records

**Cons**:
- DDL operations briefly lock `pg_class` catalog (negligible for CREATE/DROP)
- Application needs DDL permissions (not just DML)
- More tables in `pg_catalog` (manageable with cleanup)

**Alternative considered**: PostgreSQL LIST partitioning by iteration_id on a single base table per entity type. This provides similar isolation (DROP PARTITION is fast) with cleaner DDL management. Worth revisiting if dynamic table management proves cumbersome, but the straightforward approach is simpler to implement and reason about initially.
