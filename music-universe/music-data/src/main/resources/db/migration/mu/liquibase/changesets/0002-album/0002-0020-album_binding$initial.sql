CREATE TABLE album_binding (
        id                      BIGINT      NOT NULL
    ,   reference_id            BIGINT      NOT NULL
    ,   data_source_id          SMALLINT    NOT NULL
    ,   external_id             BIGINT      NOT NULL
    ,   created_at              TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
    ,   updated_at              TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
    ,   CONSTRAINT album_binding_PK
            PRIMARY KEY (id)
    ,   CONSTRAINT album_binding_FK_album
            FOREIGN KEY (reference_id)
                REFERENCES album(id)
                ON DELETE CASCADE
);
CREATE SEQUENCE album_binding_seq START 1 INCREMENT BY 50;

CREATE UNIQUE INDEX album_binding_UI_external_album
    ON album_binding (external_id, data_source_id);

CREATE INDEX album_binding_I_reference_album
    ON album_binding (reference_id);

CREATE INDEX album_binding_I_data_source
    ON album_binding (data_source_id);
