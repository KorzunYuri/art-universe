CREATE TABLE category (
        id                  BIGINT          NOT NULL
    ,   name                VARCHAR(255)    NOT NULL
    ,   dimension_id        BIGINT
    ,   parent_id           BIGINT
    ,   created_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
    ,   updated_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
    ,   CONSTRAINT category_PK
            PRIMARY KEY (id)
    ,   CONSTRAINT category_FK_dimension
            FOREIGN KEY (dimension_id)
                REFERENCES dimension(id)
                ON DELETE SET NULL
    ,   CONSTRAINT category_FK_parent
            FOREIGN KEY (parent_id)
                REFERENCES category(id)
                ON DELETE CASCADE
);
CREATE SEQUENCE category_seq START 1 INCREMENT BY 50;

CREATE INDEX category_I_dimension
    ON category (dimension_id);

CREATE INDEX category_I_parent
    ON category (parent_id);
