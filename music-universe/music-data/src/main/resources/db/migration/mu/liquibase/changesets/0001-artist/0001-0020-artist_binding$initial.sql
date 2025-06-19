CREATE TABLE artist_binding (
        id                      BIGINT      NOT NULL
    ,   reference_id            BIGINT      NOT NULL
    ,   data_source_id          SMALLINT    NOT NULL
    ,   external_id             BIGINT      NOT NULL
    ,   created_at              TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
    ,   updated_at              TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
    ,   CONSTRAINT artist_binding_PK
            PRIMARY KEY (id)
);
CREATE SEQUENCE artist_binding_seq START 1 INCREMENT BY 50;

CREATE UNIQUE INDEX artist_binding_UI_external_artist
    ON artist_binding (external_id, data_source_id);

CREATE INDEX artist_binding_I_reference_artist
    ON artist_binding (reference_id);

CREATE INDEX artist_binding_I_data_source
    ON artist_binding (data_source_id);
