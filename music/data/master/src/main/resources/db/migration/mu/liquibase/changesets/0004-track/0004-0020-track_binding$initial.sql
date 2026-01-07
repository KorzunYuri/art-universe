CREATE TABLE track_binding (
        id                      BIGINT      NOT NULL
    ,   reference_id            BIGINT      NOT NULL
    ,   data_source_id          SMALLINT    NOT NULL
    ,   external_id             BIGINT      NOT NULL
    ,   created_at              TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
    ,   updated_at              TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
    ,   CONSTRAINT track_binding_PK
                                   PRIMARY KEY (id)
    ,   CONSTRAINT track_binding_FK_track
                                   FOREIGN KEY (reference_id)
                                       REFERENCES track(id)
                                       ON DELETE CASCADE
);
CREATE SEQUENCE track_binding_seq START 1 INCREMENT BY 50;

CREATE UNIQUE INDEX track_binding_UI_external_track
    ON track_binding (external_id, data_source_id);

CREATE INDEX track_binding_I_reference_track
    ON track_binding (reference_id);

CREATE INDEX track_binding_I_data_source
    ON track_binding (data_source_id);
