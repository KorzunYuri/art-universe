# Database Schema

Back to [main plan](SPOTIFY_IMPLEMENTATION_PLAN.md#8-database-schema)

## Database: `mu_raw_spotify`

Separate database from LastFM (`mu_raw_lastfm`), following the same isolation pattern. Managed by Liquibase with changesets in `spotify-liquibase-resources`.

## Entity Tables

### artist

```sql
CREATE TABLE artist (
    id              BIGINT PRIMARY KEY DEFAULT nextval('artist_seq'),
    spotify_id      VARCHAR(64) NOT NULL UNIQUE,
    name            VARCHAR(1024) NOT NULL,
    genres          TEXT,                    -- JSON array: ["rock", "indie"]
    images          TEXT,                    -- JSON array: [{url, width, height}, ...]
    external_urls   TEXT,                    -- JSON: {"spotify": "https://open.spotify.com/artist/..."}
    uri             VARCHAR(256),            -- spotify:artist:xxxx
    type            VARCHAR(32) DEFAULT 'artist',

    api_response_id BIGINT,                 -- FK → api_response (provenance)
    approval_status SMALLINT NOT NULL DEFAULT 0,
    created_dttm    TIMESTAMPTZ NOT NULL DEFAULT now(),
    modified_dttm   TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE SEQUENCE artist_seq INCREMENT BY 50;
CREATE INDEX idx_artist_name ON artist (name);
CREATE INDEX idx_artist_approval ON artist (approval_status);
```

### album

```sql
CREATE TABLE album (
    id                  BIGINT PRIMARY KEY DEFAULT nextval('album_seq'),
    spotify_id          VARCHAR(64) NOT NULL UNIQUE,
    name                VARCHAR(1024) NOT NULL,
    album_type          VARCHAR(32),         -- "album", "single", "compilation"
    total_tracks        INTEGER,
    release_date        VARCHAR(10),         -- "2024", "2024-03", or "2024-03-15"
    release_date_precision VARCHAR(5),       -- "year", "month", "day"
    images              TEXT,                -- JSON array
    external_urls       TEXT,                -- JSON
    uri                 VARCHAR(256),

    -- Primary artist (simplified — full artist list in entity_relation)
    primary_artist_id   BIGINT,              -- FK → artist (nullable until resolved)
    primary_artist_spotify_id VARCHAR(64),   -- Kept for resolution fallback

    api_response_id     BIGINT,
    approval_status     SMALLINT NOT NULL DEFAULT 0,
    created_dttm        TIMESTAMPTZ NOT NULL DEFAULT now(),
    modified_dttm       TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE SEQUENCE album_seq INCREMENT BY 50;
CREATE INDEX idx_album_name ON album (name);
CREATE INDEX idx_album_primary_artist ON album (primary_artist_id);
CREATE INDEX idx_album_release_date ON album (release_date);
```

### track

```sql
CREATE TABLE track (
    id                  BIGINT PRIMARY KEY DEFAULT nextval('track_seq'),
    spotify_id          VARCHAR(64) NOT NULL UNIQUE,
    name                VARCHAR(1024) NOT NULL,
    duration_ms         INTEGER,
    track_number        INTEGER,
    disc_number         INTEGER,
    explicit            BOOLEAN,
    is_playable         BOOLEAN,
    external_urls       TEXT,                -- JSON
    uri                 VARCHAR(256),
    preview_url         VARCHAR(1024),

    -- Primary artist
    primary_artist_id   BIGINT,
    primary_artist_spotify_id VARCHAR(64),

    -- Album reference
    album_id            BIGINT,
    album_spotify_id    VARCHAR(64),

    api_response_id     BIGINT,
    approval_status     SMALLINT NOT NULL DEFAULT 0,
    created_dttm        TIMESTAMPTZ NOT NULL DEFAULT now(),
    modified_dttm       TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE SEQUENCE track_seq INCREMENT BY 50;
CREATE INDEX idx_track_name ON track (name);
CREATE INDEX idx_track_primary_artist ON track (primary_artist_id);
CREATE INDEX idx_track_album ON track (album_id);
```

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

    created_dttm    TIMESTAMPTZ NOT NULL DEFAULT now(),
    modified_dttm   TIMESTAMPTZ NOT NULL DEFAULT now()
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
    response_body   TEXT,                    -- Raw JSON response
    http_status     INTEGER,
    error_message   VARCHAR(1024),

    created_dttm    TIMESTAMPTZ NOT NULL DEFAULT now(),
    modified_dttm   TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE SEQUENCE api_response_seq INCREMENT BY 50;
CREATE INDEX idx_api_response_status ON api_response (status);
CREATE INDEX idx_api_response_call ON api_response (api_call_id);
```

## Relation & History Tables

### entity_relation

```sql
CREATE TABLE entity_relation (
    id                  BIGINT PRIMARY KEY DEFAULT nextval('entity_relation_seq'),
    source_entity_type  SMALLINT NOT NULL,
    source_entity_id    BIGINT NOT NULL,
    target_entity_type  SMALLINT NOT NULL,
    target_entity_id    BIGINT NOT NULL,
    relation_type       SMALLINT NOT NULL,   -- Coded enum (e.g., ARTIST_ALBUM, ALBUM_TRACK, TRACK_ARTIST)

    api_response_id     BIGINT,
    created_dttm        TIMESTAMPTZ NOT NULL DEFAULT now(),
    modified_dttm       TIMESTAMPTZ NOT NULL DEFAULT now(),

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

    api_response_id     BIGINT,
    collection_ts       TIMESTAMPTZ,
    valid_from          DATE NOT NULL,

    UNIQUE (entity_type, entity_id, attribute_id, scope_entity_type, scope_entity_id)
);

CREATE TABLE attribute_history_archive (
    -- Same columns as current, plus:
    valid_till          DATE NOT NULL
    -- No uniqueness constraint (multiple historical values per entity/attribute)
);
```

## Staging Tables (Iteration-Based)

Staging tables are created dynamically per iteration from template tables. See [staging-layer-design.md](staging-layer-design.md) for the full lifecycle.

### Template Tables

Templates serve as DDL source for dynamic table creation. Never written to directly.

```sql
-- Template for artist staging (analogous templates for album, track, entity_relation, attribute_history)
CREATE TABLE stg_artist_template (
    id              BIGSERIAL PRIMARY KEY,
    api_response_id BIGINT NOT NULL,
    staged_at       TIMESTAMPTZ DEFAULT now(),

    -- Synthetic or real ID
    entity_id       BIGINT,
    spotify_id      VARCHAR(64) NOT NULL,

    -- Mirror of target columns
    name            VARCHAR(1024),
    genres          TEXT,
    images          TEXT,
    external_urls   TEXT,
    uri             VARCHAR(256),
    type            VARCHAR(32),

    -- Per-iteration dedup: last writer wins
    UNIQUE (spotify_id)
);
```

Dynamic creation per iteration:
```sql
-- When opening iteration 42:
CREATE TABLE stg_artist_00042 (LIKE stg_artist_template INCLUDING ALL);
CREATE TABLE stg_album_00042 (LIKE stg_album_template INCLUDING ALL);
-- ... etc for all entity types
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
    spotify_id      VARCHAR(64) NOT NULL,
    synthetic_id    BIGINT NOT NULL,
    real_id         BIGINT NOT NULL,
    resolved_at     TIMESTAMPTZ DEFAULT now(),
    PRIMARY KEY (entity_type, spotify_id)
);

CREATE INDEX idx_resolution_synthetic ON synthetic_id_resolution (synthetic_id);
CREATE INDEX idx_resolution_real ON synthetic_id_resolution (real_id);
```

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

### SpotifyRelationType (coded enum)

| Code | Name | Source → Target |
|------|------|----------------|
| 1 | ARTIST_ALBUM | Artist → Album (primary artist) |
| 2 | ALBUM_ARTIST | Album → Artist (contributing artist) |
| 3 | ALBUM_TRACK | Album → Track |
| 4 | TRACK_ARTIST | Track → Artist (contributing/featured) |

## Schema Comparison with LastFM

| Table | LastFM | Spotify | Differences |
|-------|--------|---------|-------------|
| artist | name, mbid, url, listeners_count, play_count, is_primary | spotify_id, name, genres, images, uri | Spotify has genres on artist level; no popularity metrics |
| album | name, mbid, url, play_count | spotify_id, name, album_type, release_date, total_tracks | Spotify has album type and release date natively |
| track | name, mbid, url, play_count, duration | spotify_id, name, duration_ms, track_number, disc_number, explicit | Spotify has richer track metadata |
| tag | name, usage_count, usage_users_count | N/A | No equivalent — genres are embedded on artist |
| api_call | type, params, status, due_dttm, entity_type, entity_id, data_snapshot_id | + spotify_id, priority, kafka_produced, kafka_topic | Added Kafka tracking and priority |
| entity_relation | generic entity pairs | Same pattern | Same |
| attribute_history | SCD2 with staging | Same pattern | Same |
| staging tables | attribute_history only (2 fixed A/B tables) | All entity types, iteration-based (dynamic tables per batch) | Broader scope, failure isolation, audit trail |
