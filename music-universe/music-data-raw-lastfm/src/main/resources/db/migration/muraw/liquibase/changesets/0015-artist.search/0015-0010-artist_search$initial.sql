CREATE TABLE artist_search (
        id                  BIGSERIAL                           NOT NULL
    ,   search_string       VARCHAR(64)                         NOT NULL
    ,   is_processed        BOOLEAN   DEFAULT FALSE             NOT NULL
    ,   created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
    ,   updated_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
    ,   CONSTRAINT artist_search_PK PRIMARY KEY (id)
);
CREATE SEQUENCE artist_search_seq START 1 INCREMENT BY 50;