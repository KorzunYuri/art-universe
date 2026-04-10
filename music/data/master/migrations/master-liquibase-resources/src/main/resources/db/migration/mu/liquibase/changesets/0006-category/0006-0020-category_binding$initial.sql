CREATE TABLE category_binding (
        id                      BIGINT      NOT NULL
    ,   reference_id            BIGINT      NOT NULL
    ,   data_source_id          SMALLINT    NOT NULL
    ,   external_id             BIGINT      NOT NULL
    ,   created_at              TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
    ,   updated_at              TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
    ,   CONSTRAINT category_binding_PK
            PRIMARY KEY (id)
);
CREATE SEQUENCE category_binding_seq START 1 INCREMENT BY 50;

CREATE UNIQUE INDEX category_binding_UI_external_category
    ON category_binding (external_id, data_source_id);

CREATE INDEX category_binding_I_reference_category
    ON category_binding (reference_id);

CREATE INDEX category_binding_I_data_source
    ON category_binding (data_source_id);
