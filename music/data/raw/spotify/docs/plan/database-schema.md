# Database Schema

Back to [main plan](SPOTIFY_IMPLEMENTATION_PLAN.md#8-database-schema)

## Database: `mu_raw_spotify`

Separate database from LastFM (`mu_raw_lastfm`), following the same isolation pattern. Managed by Liquibase with changesets in `spotify-liquibase-resources`.

## Entity Hierarchy (JPA)

Following the project convention established in `commons-jpa` and LastFM:

```
BaseEntity (commons-jpa)                    — created_at, updated_at
  └─ BaseSpotifyCollectable (spotify-models) — approval_status, api_call reference
      └─ BaseSpotifyEntity (spotify-models)  — id, spotify_id, name, getEntityType()
          ├─ SpotifyArtist
          ├─ SpotifyAlbum
          ├─ SpotifyTrack
          └─ SpotifyGenre
```

### On BaseCollectableEntity in commons

`BaseCollectableEntity` exists in `data-raw-commons-jpa` but is currently unused — the LastFM module has its own parallel `BaseLastfmCollectable`. The commons version includes an abstract `getEntityType()` and doesn't implement `Approvable`. The LastFM version implements `Approvable` but embeds a `LastfmApiCall` field, coupling it to the LastFM domain.

**Recommendation**: Consolidate into a clean commons base before Spotify implementation:
- Make `BaseCollectableEntity` implement `Approvable`
- Remove `getEntityType()` (entity type is a domain concern, not a base-class concern)
- Remove any source-specific API call coupling (each domain adds its own `apiCall` field)
- Have both `BaseLastfmCollectable` and `BaseSpotifyCollectable` extend the fixed commons class
- This avoids duplicating approval logic across data sources

### On api_call_id vs api_response_id

LastFM entities reference `api_call_id`, not `api_response_id`. Reasons to keep `api_call_id`:
- Tracks **intent**: "this entity was collected because of this API call"
- The API call→response relationship is 1:1, so the response is always reachable via the call
- Simpler for the Generator: it can check if an entity was already fetched by looking at the entity's `api_call_id` without joining to `api_response`
- Consistent with LastFM, reducing cognitive overhead

For Spotify we follow the same convention: **entities reference `api_call_id`**.

The `api_response_id` is still recorded in staging records (provenance: "which response body was parsed to produce this staged record") and in `attribute_history` (for debugging value changes). But the primary entity→call link uses `api_call_id`.

---

## Entity Tables

### artist

```sql
CREATE TABLE artist (
    id              BIGINT PRIMARY KEY DEFAULT nextval('artist_seq'),
    spotify_id      VARCHAR(64) NOT NULL UNIQUE,
    name            VARCHAR(1024) NOT NULL,
    spotify_url     VARCHAR(512),            -- Parsed from external_urls.spotify
    uri             VARCHAR(256),            -- spotify:artist:xxxx

    api_call_id     BIGINT,                  -- FK → api_call (provenance)
    approval_status SMALLINT NOT NULL DEFAULT 1,  -- ApprovalStatus coded enum (1=PENDING)
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE SEQUENCE artist_seq INCREMENT BY 50;
CREATE INDEX idx_artist_name ON artist (name);
CREATE INDEX idx_artist_approval ON artist (approval_status);
```

**Changes from initial draft**:
- `genres` → removed; genre is now a separate entity (see below)
- `images` → removed (not needed)
- `external_urls` (JSON) → `spotify_url` (VARCHAR, parsed from `external_urls.spotify`)
- `type` → removed (always "artist", no value in storing)
- `api_response_id` → `api_call_id` (consistent with LastFM convention)
- `created_dttm`/`modified_dttm` → `created_at`/`updated_at` (consistent with `BaseEntity`)

### genre

Genre is a **fourth entity type**, equivalent to LastFM's `tag` entity. Spotify genres are curated strings on artist objects. We extract them into a standalone entity for:
- Binding to master categories
- Normalization (many artists share genres)
- Tracking genre changes over time via attribute_history

```sql
CREATE TABLE genre (
    id              BIGINT PRIMARY KEY DEFAULT nextval('genre_seq'),
    spotify_id      VARCHAR(256) NOT NULL UNIQUE,  -- Genre name IS the Spotify ID (no separate ID exists)
    name            VARCHAR(256) NOT NULL,          -- Same as spotify_id; kept for BaseSpotifyEntity contract

    api_call_id     BIGINT,                  -- FK → api_call (which call first discovered this genre)
    approval_status SMALLINT NOT NULL DEFAULT 1,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE SEQUENCE genre_seq INCREMENT BY 50;
CREATE UNIQUE INDEX idx_genre_name ON genre (name);
```

**Notes**:
- Spotify genres have no dedicated ID — the genre string itself (e.g., "indie rock") serves as the canonical key
- `spotify_id` = `name` for genres (the name IS the identifier)
- Artist↔Genre relationship stored in `entity_relation` table
- Genre entity will have an `ARTIST_GENRE` relation type in `SpotifyRelationType`

### album

```sql
CREATE TABLE album (
    id                  BIGINT PRIMARY KEY DEFAULT nextval('album_seq'),
    spotify_id          VARCHAR(64) NOT NULL UNIQUE,
    name                VARCHAR(1024) NOT NULL,
    album_type          SMALLINT,            -- SpotifyAlbumType coded enum (1=ALBUM, 2=SINGLE, 3=COMPILATION)
    total_tracks        INTEGER,
    release_date        VARCHAR(10),         -- "2024", "2024-03", or "2024-03-15"
    release_date_precision SMALLINT,         -- SpotifyDatePrecision coded enum (1=YEAR, 2=MONTH, 3=DAY)
    spotify_url         VARCHAR(512),        -- Parsed from external_urls.spotify
    uri                 VARCHAR(256),

    -- Primary artist (simplified — full artist list in entity_relation)
    primary_artist_id   BIGINT,              -- FK → artist (nullable until resolved)
    primary_artist_spotify_id VARCHAR(64),   -- Kept for staging resolution fallback

    api_call_id         BIGINT,
    approval_status     SMALLINT NOT NULL DEFAULT 1,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE SEQUENCE album_seq INCREMENT BY 50;
CREATE INDEX idx_album_name ON album (name);
CREATE INDEX idx_album_primary_artist ON album (primary_artist_id);
CREATE INDEX idx_album_release_date ON album (release_date);
CREATE INDEX idx_album_type ON album (album_type);
```

**Changes from initial draft**:
- `album_type` VARCHAR → SMALLINT coded enum
- `release_date_precision` VARCHAR → SMALLINT coded enum
- `images` → removed
- `external_urls` → `spotify_url`

### track

```sql
CREATE TABLE track (
    id                  BIGINT PRIMARY KEY DEFAULT nextval('track_seq'),
    spotify_id          VARCHAR(64) NOT NULL UNIQUE,
    name                VARCHAR(1024) NOT NULL,
    duration_ms         INTEGER,
    track_number        INTEGER,
    disc_number         INTEGER,
    has_explicit_lyrics BOOLEAN,
    is_playable         BOOLEAN,
    spotify_url         VARCHAR(512),        -- Parsed from external_urls.spotify
    uri                 VARCHAR(256),

    -- External IDs (deprecated in API but still available)
    isrc                VARCHAR(12),         -- International Standard Recording Code
    ean                 VARCHAR(13),         -- International Article Number
    upc                 VARCHAR(12),         -- Universal Product Code

    -- Primary artist
    primary_artist_id   BIGINT,
    primary_artist_spotify_id VARCHAR(64),

    -- Album reference
    album_id            BIGINT,
    album_spotify_id    VARCHAR(64),

    api_call_id         BIGINT,
    approval_status     SMALLINT NOT NULL DEFAULT 1,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE SEQUENCE track_seq INCREMENT BY 50;
CREATE INDEX idx_track_name ON track (name);
CREATE INDEX idx_track_primary_artist ON track (primary_artist_id);
CREATE INDEX idx_track_album ON track (album_id);
CREATE INDEX idx_track_isrc ON track (isrc) WHERE isrc IS NOT NULL;
```

**Changes from initial draft**:
- `explicit` → `has_explicit_lyrics` (clearer naming)
- `external_urls` → `spotify_url`
- `preview_url` → removed (deprecated)
- Added `isrc`, `ean`, `upc` fields from `external_ids` (deprecated in API but still available — valuable for cross-source matching)

---

## API Infrastructure Tables

### api_call

```sql
CREATE TABLE api_call (
    id              BIGINT PRIMARY KEY DEFAULT nextval('api_call_seq'),
    type            SMALLINT NOT NULL,       -- SpotifyApiCallType coded enum
    status          SMALLINT NOT NULL DEFAULT 0,  -- ApiCallStatus

    -- Parameters
    spotify_id      VARCHAR(64),             -- Target entity's Spotify ID
    params          TEXT,                    -- JSON: additional params (e.g., search query)

    -- Scoping
    entity_type     SMALLINT,               -- SpotifyEntityType
    entity_id       BIGINT,                 -- Reference to raw entity (if exists)

    -- Scheduling
    due_dttm        TIMESTAMPTZ NOT NULL DEFAULT now(),
    priority        SMALLINT NOT NULL DEFAULT 0,  -- 0=NORMAL, 1=HIGH (seed)

    -- Kafka tracking
    kafka_produced  BOOLEAN DEFAULT false,
    kafka_topic     VARCHAR(64),

    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE SEQUENCE api_call_seq INCREMENT BY 50;
CREATE INDEX idx_api_call_status_type ON api_call (status, type);
CREATE INDEX idx_api_call_due ON api_call (due_dttm) WHERE status IN (0, 1);
CREATE INDEX idx_api_call_spotify_id ON api_call (spotify_id, type);
```

### api_response

```sql
CREATE TABLE api_response (
    id              BIGINT PRIMARY KEY DEFAULT nextval('api_response_seq'),
    api_call_id     BIGINT NOT NULL REFERENCES api_call(id),
    status          SMALLINT NOT NULL DEFAULT 0,  -- ApiResponseStatus
    response_body   TEXT,                    -- GZIP + Base64 encoded (via GzipBase64StringConverter)
    http_status     INTEGER,
    error_message   VARCHAR(1024),

    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE SEQUENCE api_response_seq INCREMENT BY 50;
CREATE INDEX idx_api_response_status ON api_response (status);
CREATE INDEX idx_api_response_call ON api_response (api_call_id);
```

**Note**: `response_body` is stored using `GzipBase64StringConverter` (same as LastFM's `LastfmApiResponse`). Raw JSON is compressed to ~10-15% of original size. The JPA entity uses `@Convert(converter = GzipBase64StringConverter.class)` so application code works with plain strings.

---

## Relation & History Tables

### entity_relation

```sql
CREATE TABLE entity_relation (
    id                  BIGINT PRIMARY KEY DEFAULT nextval('entity_relation_seq'),
    source_entity_type  SMALLINT NOT NULL,
    source_entity_id    BIGINT NOT NULL,
    target_entity_type  SMALLINT NOT NULL,
    target_entity_id    BIGINT NOT NULL,
    relation_type       SMALLINT NOT NULL,   -- SpotifyRelationType coded enum

    api_call_id         BIGINT,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now(),

    UNIQUE (source_entity_type, source_entity_id, target_entity_type, target_entity_id, relation_type)
);

CREATE SEQUENCE entity_relation_seq INCREMENT BY 50;
CREATE INDEX idx_rel_source ON entity_relation (source_entity_type, source_entity_id);
CREATE INDEX idx_rel_target ON entity_relation (target_entity_type, target_entity_id);
```

### attribute_history_current / attribute_history_archive

Same structure as LastFM's attribute history (SCD2 pattern). Reused as-is since it's a proven design.

```sql
CREATE TABLE attribute_history_current (
    id                  BIGINT PRIMARY KEY DEFAULT nextval('attr_hist_seq'),
    entity_type         SMALLINT NOT NULL,
    entity_id           BIGINT NOT NULL,
    attribute_id        SMALLINT NOT NULL,
    scope_entity_type   SMALLINT,
    scope_entity_id     BIGINT,

    string_value        VARCHAR(4096),
    numeric_value       BIGINT,

    api_call_id         BIGINT,
    collection_ts       TIMESTAMPTZ,
    valid_from          DATE NOT NULL,

    UNIQUE (entity_type, entity_id, attribute_id, scope_entity_type, scope_entity_id)
);

CREATE TABLE attribute_history_archive (
    -- Same columns as current, plus:
    valid_till          DATE NOT NULL
    -- No uniqueness constraint (multiple historical values per entity/attribute)
);

CREATE SEQUENCE attr_hist_seq INCREMENT BY 50;
```

---

## Staging Tables (Iteration-Based)

Staging tables are created dynamically per iteration from template tables. See [staging-layer-design.md](staging-layer-design.md) for the full lifecycle.

### Template Tables

Templates serve as DDL source for dynamic table creation. Never written to directly. Each template mirrors its target entity columns plus staging metadata.

```sql
-- Artist staging template
CREATE TABLE stg_artist_template (
    id              BIGSERIAL PRIMARY KEY,
    api_response_id BIGINT NOT NULL,         -- Provenance: which response produced this record
    staged_at       TIMESTAMPTZ DEFAULT now(),

    entity_id       BIGINT,                  -- Synthetic (negative) or real (positive) ID
    spotify_id      VARCHAR(64) NOT NULL,
    name            VARCHAR(1024),
    spotify_url     VARCHAR(512),
    uri             VARCHAR(256),

    UNIQUE (spotify_id)
);

-- Genre staging template
CREATE TABLE stg_genre_template (
    id              BIGSERIAL PRIMARY KEY,
    api_response_id BIGINT NOT NULL,
    staged_at       TIMESTAMPTZ DEFAULT now(),

    entity_id       BIGINT,
    spotify_id      VARCHAR(256) NOT NULL,   -- Genre name as ID
    name            VARCHAR(256),

    UNIQUE (spotify_id)
);

-- Album staging template
CREATE TABLE stg_album_template (
    id              BIGSERIAL PRIMARY KEY,
    api_response_id BIGINT NOT NULL,
    staged_at       TIMESTAMPTZ DEFAULT now(),

    entity_id       BIGINT,
    spotify_id      VARCHAR(64) NOT NULL,
    name            VARCHAR(1024),
    album_type      SMALLINT,
    total_tracks    INTEGER,
    release_date    VARCHAR(10),
    release_date_precision SMALLINT,
    spotify_url     VARCHAR(512),
    uri             VARCHAR(256),
    primary_artist_id BIGINT,                -- Synthetic ID until resolved
    primary_artist_spotify_id VARCHAR(64),

    UNIQUE (spotify_id)
);

-- Track staging template
CREATE TABLE stg_track_template (
    id              BIGSERIAL PRIMARY KEY,
    api_response_id BIGINT NOT NULL,
    staged_at       TIMESTAMPTZ DEFAULT now(),

    entity_id       BIGINT,
    spotify_id      VARCHAR(64) NOT NULL,
    name            VARCHAR(1024),
    duration_ms     INTEGER,
    track_number    INTEGER,
    disc_number     INTEGER,
    has_explicit_lyrics BOOLEAN,
    is_playable     BOOLEAN,
    spotify_url     VARCHAR(512),
    uri             VARCHAR(256),
    isrc            VARCHAR(12),
    ean             VARCHAR(13),
    upc             VARCHAR(12),
    primary_artist_id BIGINT,
    primary_artist_spotify_id VARCHAR(64),
    album_id        BIGINT,
    album_spotify_id VARCHAR(64),

    UNIQUE (spotify_id)
);

-- Entity relation and attribute_history staging templates follow the same pattern
-- as their target tables, plus (id, api_response_id, staged_at) metadata columns
```

Dynamic creation per iteration:
```sql
-- When opening iteration 42:
CREATE TABLE stg_artist_00042 (LIKE stg_artist_template INCLUDING ALL);
CREATE TABLE stg_genre_00042 (LIKE stg_genre_template INCLUDING ALL);
CREATE TABLE stg_album_00042 (LIKE stg_album_template INCLUDING ALL);
CREATE TABLE stg_track_00042 (LIKE stg_track_template INCLUDING ALL);
CREATE TABLE stg_entity_relation_00042 (LIKE stg_entity_relation_template INCLUDING ALL);
CREATE TABLE stg_attribute_history_00042 (LIKE stg_attribute_history_template INCLUDING ALL);
```

### Staging Iteration Metadata

```sql
CREATE TABLE staging_iteration (
    id              BIGINT PRIMARY KEY DEFAULT nextval('staging_iteration_seq'),
    status          SMALLINT NOT NULL DEFAULT 0,
    -- 0=OPEN, 1=SEALED, 2=APPLYING, 3=COMPLETED, 4=FAILED, 5=RETRYING

    records_staged  BIGINT DEFAULT 0,
    records_applied BIGINT DEFAULT 0,
    records_failed  BIGINT DEFAULT 0,

    opened_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    sealed_at       TIMESTAMPTZ,
    applied_at      TIMESTAMPTZ,
    error_message   TEXT,

    retry_count     SMALLINT DEFAULT 0,
    last_retry_at   TIMESTAMPTZ
);

CREATE SEQUENCE staging_iteration_seq INCREMENT BY 1;
```

### Synthetic ID Resolution

```sql
CREATE TABLE synthetic_id_resolution (
    entity_type     SMALLINT NOT NULL,
    spotify_id      VARCHAR(256) NOT NULL,   -- VARCHAR(256) to accommodate genre names
    synthetic_id    BIGINT NOT NULL,
    real_id         BIGINT NOT NULL,
    resolved_at     TIMESTAMPTZ DEFAULT now(),
    PRIMARY KEY (entity_type, spotify_id)
);

CREATE INDEX idx_resolution_synthetic ON synthetic_id_resolution (synthetic_id);
CREATE INDEX idx_resolution_real ON synthetic_id_resolution (real_id);
```

---

## Enums

### SpotifyApiCallType (coded enum)

| Code | Name | Spotify Endpoint | Params |
|------|------|-----------------|--------|
| 1 | ARTIST_GET | `GET /artists/{id}` | spotify_id |
| 2 | ARTIST_ALBUMS | `GET /artists/{id}/albums` | spotify_id, offset, limit |
| 3 | ALBUM_GET | `GET /albums/{id}` | spotify_id |
| 4 | ALBUM_TRACKS | `GET /albums/{id}/tracks` | spotify_id, offset, limit |
| 5 | TRACK_GET | `GET /tracks/{id}` | spotify_id |
| 6 | SEARCH_ARTIST | `GET /search?type=artist` | query |
| 7 | SEARCH_ALBUM | `GET /search?type=album` | query |
| 8 | SEARCH_TRACK | `GET /search?type=track` | query |

### SpotifyEntityType (coded enum)

| Code | Name | Table |
|------|------|-------|
| 1 | ARTIST | artist |
| 2 | ALBUM | album |
| 3 | TRACK | track |
| 4 | GENRE | genre |

### SpotifyAlbumType (coded enum)

| Code | Name |
|------|------|
| 1 | ALBUM |
| 2 | SINGLE |
| 3 | COMPILATION |

### SpotifyDatePrecision (coded enum)

| Code | Name |
|------|------|
| 1 | YEAR |
| 2 | MONTH |
| 3 | DAY |

### SpotifyRelationType (coded enum)

| Code | Name | Source → Target |
|------|------|----------------|
| 1 | ARTIST_ALBUM | Artist → Album (primary artist) |
| 2 | ALBUM_ARTIST | Album → Artist (contributing artist) |
| 3 | ALBUM_TRACK | Album → Track |
| 4 | TRACK_ARTIST | Track → Artist (contributing/featured) |
| 5 | ARTIST_GENRE | Artist → Genre |

---

## Schema Comparison with LastFM

| Table | LastFM | Spotify | Differences |
|-------|--------|---------|-------------|
| artist | name, mbid, url, listeners_count, play_count, is_primary | spotify_id, name, spotify_url, uri | Spotify has no popularity metrics; genres moved to separate entity |
| album | name, mbid, url, play_count | spotify_id, name, album_type (enum), release_date, total_tracks | Spotify has album type and release date natively |
| track | name, mbid, url, play_count, duration | spotify_id, name, duration_ms, track_number, disc_number, has_explicit_lyrics, isrc/ean/upc | Spotify has richer metadata + external IDs for cross-source matching |
| tag / genre | name, usage_count, usage_users_count | spotify_id (=name), name | Spotify genres are curated (not user-generated); will be bound to master categories |
| api_call | type, params, status, due_dttm, entity_type, entity_id, data_snapshot_id | + spotify_id, priority, kafka_produced, kafka_topic | Added Kafka tracking and priority |
| api_response | response_body (gzip+base64) | Same (gzip+base64 via GzipBase64StringConverter) | Same compression approach |
| entity_relation | generic entity pairs | Same pattern + ARTIST_GENRE relation type | Same |
| attribute_history | SCD2 with staging | Same pattern | Same |
| staging tables | attribute_history only (2 fixed A/B tables) | All entity types, iteration-based (dynamic tables per batch) | Broader scope, failure isolation, audit trail |
