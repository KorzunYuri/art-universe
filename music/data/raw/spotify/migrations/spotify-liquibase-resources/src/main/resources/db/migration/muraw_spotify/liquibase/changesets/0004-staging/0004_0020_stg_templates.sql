-- Artist staging template
CREATE TABLE stg_artist_template (
        id              BIGSERIAL PRIMARY KEY
    ,   api_response_id BIGINT                              NOT NULL
    ,   staged_at       TIMESTAMPTZ                         DEFAULT now()
    ,   entity_id       BIGINT
    ,   spotify_id      VARCHAR(64)                         NOT NULL
    ,   name            VARCHAR(1024)
    ,   spotify_url     VARCHAR(512)
    ,   uri             VARCHAR(256)
    ,   UNIQUE (spotify_id)
);

-- Genre staging template
CREATE TABLE stg_genre_template (
        id              BIGSERIAL PRIMARY KEY
    ,   api_response_id BIGINT                              NOT NULL
    ,   staged_at       TIMESTAMPTZ                         DEFAULT now()
    ,   entity_id       BIGINT
    ,   spotify_id      VARCHAR(256)                        NOT NULL
    ,   name            VARCHAR(256)
    ,   UNIQUE (spotify_id)
);

-- Album staging template
CREATE TABLE stg_album_template (
        id              BIGSERIAL PRIMARY KEY
    ,   api_response_id BIGINT                              NOT NULL
    ,   staged_at       TIMESTAMPTZ                         DEFAULT now()
    ,   entity_id       BIGINT
    ,   spotify_id      VARCHAR(64)                         NOT NULL
    ,   name            VARCHAR(1024)
    ,   album_type      SMALLINT
    ,   total_tracks    INTEGER
    ,   release_date    VARCHAR(10)
    ,   release_date_precision SMALLINT
    ,   spotify_url     VARCHAR(512)
    ,   uri             VARCHAR(256)
    ,   primary_artist_id BIGINT
    ,   primary_artist_spotify_id VARCHAR(64)
    ,   UNIQUE (spotify_id)
);

-- Track staging template
CREATE TABLE stg_track_template (
        id              BIGSERIAL PRIMARY KEY
    ,   api_response_id BIGINT                              NOT NULL
    ,   staged_at       TIMESTAMPTZ                         DEFAULT now()
    ,   entity_id       BIGINT
    ,   spotify_id      VARCHAR(64)                         NOT NULL
    ,   name            VARCHAR(1024)
    ,   duration_ms     INTEGER
    ,   track_number    INTEGER
    ,   disc_number     INTEGER
    ,   has_explicit_lyrics BOOLEAN
    ,   is_playable     BOOLEAN
    ,   spotify_url     VARCHAR(512)
    ,   uri             VARCHAR(256)
    ,   isrc            VARCHAR(12)
    ,   ean             VARCHAR(13)
    ,   upc             VARCHAR(12)
    ,   primary_artist_id BIGINT
    ,   primary_artist_spotify_id VARCHAR(64)
    ,   album_id        BIGINT
    ,   album_spotify_id VARCHAR(64)
    ,   UNIQUE (spotify_id)
);

-- Entity relation staging template
CREATE TABLE stg_entity_relation_template (
        id                  BIGSERIAL PRIMARY KEY
    ,   api_response_id     BIGINT                          NOT NULL
    ,   staged_at           TIMESTAMPTZ                     DEFAULT now()
    ,   source_entity_type  SMALLINT                        NOT NULL
    ,   source_entity_id    BIGINT                          NOT NULL
    ,   target_entity_type  SMALLINT                        NOT NULL
    ,   target_entity_id    BIGINT                          NOT NULL
    ,   relation_type       SMALLINT                        NOT NULL
    ,   UNIQUE (source_entity_type, source_entity_id, target_entity_type, target_entity_id, relation_type)
);

-- Attribute history staging template
CREATE TABLE stg_attribute_history_template (
        id                  BIGSERIAL PRIMARY KEY
    ,   api_response_id     BIGINT                          NOT NULL
    ,   staged_at           TIMESTAMPTZ                     DEFAULT now()
    ,   entity_type         SMALLINT                        NOT NULL
    ,   entity_id           BIGINT                          NOT NULL
    ,   attribute_id        SMALLINT                        NOT NULL
    ,   scope_entity_type   SMALLINT
    ,   scope_entity_id     BIGINT
    ,   string_value        VARCHAR(4096)
    ,   numeric_value       BIGINT
    ,   collection_ts       TIMESTAMPTZ
);
