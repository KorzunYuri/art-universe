CREATE TABLE album (
        id                      BIGINT PRIMARY KEY DEFAULT nextval('album_seq')
    ,   spotify_id              VARCHAR(64)                         NOT NULL UNIQUE
    ,   name                    VARCHAR(1024)                       NOT NULL
    ,   album_type              SMALLINT
    ,   total_tracks            INTEGER
    ,   release_date            VARCHAR(10)
    ,   release_date_precision  SMALLINT
    ,   spotify_url             VARCHAR(512)
    ,   uri                     VARCHAR(256)
    ,   primary_artist_id       BIGINT
    ,   primary_artist_spotify_id VARCHAR(64)
    ,   api_call_id             BIGINT
    ,   approval_status         SMALLINT                            NOT NULL DEFAULT 1
    ,   created_at              TIMESTAMPTZ                         NOT NULL DEFAULT now()
    ,   updated_at              TIMESTAMPTZ                         NOT NULL DEFAULT now()
);

CREATE SEQUENCE album_seq START 1 INCREMENT BY 50;

CREATE INDEX idx_album_name ON album (name);
CREATE INDEX idx_album_primary_artist ON album (primary_artist_id);
CREATE INDEX idx_album_release_date ON album (release_date);
CREATE INDEX idx_album_type ON album (album_type);
