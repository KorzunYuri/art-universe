CREATE TABLE entity_relation (
        id                      BIGINT PRIMARY KEY DEFAULT nextval('entity_relation_seq')
    ,   source_entity_type      SMALLINT                            NOT NULL
    ,   source_entity_id        BIGINT                              NOT NULL
    ,   target_entity_type      SMALLINT                            NOT NULL
    ,   target_entity_id        BIGINT                              NOT NULL
    ,   relation_type           SMALLINT                            NOT NULL
    ,   api_call_id             BIGINT
    ,   approval_status         SMALLINT                            NOT NULL DEFAULT 1
    ,   created_at              TIMESTAMPTZ                         NOT NULL DEFAULT now()
    ,   updated_at              TIMESTAMPTZ                         NOT NULL DEFAULT now()
    ,   UNIQUE (source_entity_type, source_entity_id, target_entity_type, target_entity_id, relation_type)
);

CREATE SEQUENCE entity_relation_seq START 1 INCREMENT BY 50;

CREATE INDEX idx_rel_source ON entity_relation (source_entity_type, source_entity_id);
CREATE INDEX idx_rel_target ON entity_relation (target_entity_type, target_entity_id);
