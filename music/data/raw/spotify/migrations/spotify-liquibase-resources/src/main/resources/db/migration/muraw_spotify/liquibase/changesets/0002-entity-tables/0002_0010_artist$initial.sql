CREATE TABLE artist (
        id                  BIGINT PRIMARY KEY DEFAULT nextval('artist_seq')
    ,   spotify_id          VARCHAR(64)                         NOT NULL UNIQUE
    ,   name                VARCHAR(1024)                       NOT NULL
    ,   spotify_url         VARCHAR(512)
    ,   uri                 VARCHAR(256)
    ,   api_call_id         BIGINT
    ,   approval_status     SMALLINT                            NOT NULL DEFAULT 1
    ,   created_at          TIMESTAMPTZ                         NOT NULL DEFAULT now()
    ,   updated_at          TIMESTAMPTZ                         NOT NULL DEFAULT now()
);

CREATE SEQUENCE artist_seq START 1 INCREMENT BY 50;

CREATE INDEX idx_artist_name ON artist (name);
CREATE INDEX idx_artist_approval ON artist (approval_status);
