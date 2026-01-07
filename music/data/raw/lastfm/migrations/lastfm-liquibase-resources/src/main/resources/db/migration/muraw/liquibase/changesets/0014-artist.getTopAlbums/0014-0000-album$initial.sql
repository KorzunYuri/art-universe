CREATE TABLE album (
        id                  BIGSERIAL                           NOT NULL
    ,   name                VARCHAR(64)                         NOT NULL
    ,   description         TEXT
    ,   mbid                VARCHAR(36)
    ,   url                 VARCHAR(1024)
    ,   play_count          INTEGER
    ,   listeners_count     INTEGER
    ,   approval_status     SMALLINT                            NOT NULL
    ,   publish_ts          TIMESTAMP
    ,   api_call_id         BIGINT
    ,   created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
    ,   updated_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
    ,   CONSTRAINT album_PK
            PRIMARY KEY (id)
    ,   UNIQUE (url)
);
CREATE SEQUENCE album_seq START 1 INCREMENT BY 50;