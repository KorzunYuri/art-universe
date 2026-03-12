CREATE SEQUENCE track_seq START 1 INCREMENT BY 1;

CREATE TABLE track (
        id                      BIGINT PRIMARY KEY DEFAULT nextval('track_seq')
    ,   spotify_id              VARCHAR(64)                         NOT NULL UNIQUE
    ,   name                    VARCHAR(1024)                       NOT NULL
    ,   duration_ms             INTEGER
    ,   track_number            INTEGER
    ,   disc_number             INTEGER
    ,   has_explicit_lyrics     BOOLEAN
    ,   is_playable             BOOLEAN
    ,   spotify_url             VARCHAR(512)
    ,   uri                     VARCHAR(256)
    ,   isrc                    VARCHAR(12)
    ,   ean                     VARCHAR(13)
    ,   upc                     VARCHAR(12)
    ,   primary_artist_id       BIGINT
    ,   primary_artist_spotify_id VARCHAR(64)
    ,   album_id                BIGINT
    ,   album_spotify_id        VARCHAR(64)
    ,   api_call_id             BIGINT
    ,   approval_status         SMALLINT                            NOT NULL DEFAULT 1
    ,   created_at              TIMESTAMPTZ                         NOT NULL DEFAULT now()
    ,   updated_at              TIMESTAMPTZ                         NOT NULL DEFAULT now()
);

CREATE INDEX idx_track_name ON track (name);
CREATE INDEX idx_track_primary_artist ON track (primary_artist_id);
CREATE INDEX idx_track_album ON track (album_id);
CREATE INDEX idx_track_isrc ON track (isrc) WHERE isrc IS NOT NULL;
