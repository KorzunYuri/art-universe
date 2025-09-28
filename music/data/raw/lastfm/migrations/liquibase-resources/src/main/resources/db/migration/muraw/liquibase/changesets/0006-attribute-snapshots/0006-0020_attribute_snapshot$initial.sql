CREATE TABLE attribute_snapshot (
        id                      BIGSERIAL
    ,   attribute_id            BIGINT
    ,   entity_type             SMALLINT
    ,   scope_entity_type       SMALLINT
    ,   scope_entity_id         BIGINT
    ,   data_snapshot_id_prev   BIGINT
    ,   data_snapshot_id_cur    BIGINT
    ,   created_at              TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
    ,   updated_at              TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
    ,   UNIQUE (attribute_id, entity_type, scope_entity_type, scope_entity_id)
    ,   CONSTRAINT attribute_snapshot$snapshot_id_FK
            FOREIGN KEY (data_snapshot_id_cur) REFERENCES data_snapshot(id)
    ,   CONSTRAINT attribute_snapshot$attribute_id_FK
            FOREIGN KEY (attribute_id) REFERENCES attribute(id)
);
CREATE SEQUENCE attribute_snapshot_seq START 1 INCREMENT BY 50;