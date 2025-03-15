CREATE TABLE attribute (
        id                  BIGSERIAL                           NOT NULL
    ,   name                VARCHAR(64)                         NOT NULL
    ,   description         VARCHAR(256)
    ,   type                SMALLINT                            NOT NULL
    ,   created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
    ,   updated_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
    ,   CONSTRAINT attribute_PK
            PRIMARY KEY (id)
    ,   UNIQUE (name)
);