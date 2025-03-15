CREATE TABLE tag (
        id                  BIGSERIAL                           NOT NULL
    ,   name                VARCHAR(64)                         NOT NULL
    ,   approval_status     SMALLINT                            NOT NULL
    ,   created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
    ,   updated_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
    ,   CONSTRAINT tag_PK
            PRIMARY KEY (id)
    ,   UNIQUE (name)
);
