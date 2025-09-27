CREATE TABLE artist
(
        id          BIGINT                                      NOT NULL
    ,   name        VARCHAR(1024)                               NOT NULL
    ,   created_at  TIMESTAMP       DEFAULT CURRENT_TIMESTAMP   NOT NULL
    ,   updated_at  TIMESTAMP       DEFAULT CURRENT_TIMESTAMP   NOT NULL
    ,   CONSTRAINT artist_PK
            PRIMARY KEY (id)
    ,   CONSTRAINT artist_UK
            UNIQUE (name)
);
CREATE SEQUENCE artist_seq START 1 INCREMENT BY 50;
