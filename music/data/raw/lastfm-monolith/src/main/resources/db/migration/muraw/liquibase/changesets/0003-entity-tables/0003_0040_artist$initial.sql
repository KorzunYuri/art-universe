CREATE TABLE artist (
        id                  BIGSERIAL                           NOT NULL
    ,   name                VARCHAR(64)                         NOT NULL
    ,   mbid                VARCHAR(36)
    ,   url                 VARCHAR(1024)
    ,   approval_status     SMALLINT                            NOT NULL
    ,   created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
    ,   updated_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
    ,   CONSTRAINT artist_PK
            PRIMARY KEY (id)
    ,   UNIQUE (name)
);
CREATE SEQUENCE artist_seq START 1 INCREMENT BY 50;