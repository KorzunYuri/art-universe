CREATE TABLE dimension (
        id                  BIGINT          NOT NULL
    ,   name                VARCHAR(255)    NOT NULL
    ,   created_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
    ,   updated_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
    ,   CONSTRAINT dimension_PK
            PRIMARY KEY (id)
);
CREATE SEQUENCE dimension_seq START 1 INCREMENT BY 50;

CREATE UNIQUE INDEX dimension_UI_name
    ON dimension (name);
