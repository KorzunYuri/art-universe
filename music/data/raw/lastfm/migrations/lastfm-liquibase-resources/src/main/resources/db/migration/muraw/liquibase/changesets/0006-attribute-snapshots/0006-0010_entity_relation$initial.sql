CREATE TABLE entity_relation (
        id                      BIGSERIAL
    ,   scope_entity_type       SMALLINT                            NOT NULL
    ,   scope_entity_id         BIGINT                              NOT NULL
    ,   entity_type             SMALLINT                            NOT NULL
    ,   entity_id               BIGINT                              NOT NULL
    ,   api_call_id             BIGINT                              NOT NULL
    ,   created_at              TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
    ,   updated_at              TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
    ,   CONSTRAINT entity_relation$unique
            UNIQUE (scope_entity_type, scope_entity_id, entity_type, entity_id)
    ,   CONSTRAINT entity_relation$api_call_id_FK
            FOREIGN KEY (api_call_id) REFERENCES api_call(id)
);
CREATE SEQUENCE entity_relation_seq START 1 INCREMENT BY 50;