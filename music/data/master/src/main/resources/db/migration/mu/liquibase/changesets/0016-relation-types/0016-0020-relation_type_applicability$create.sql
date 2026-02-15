-- Create relation_type_applicability table (which relation types apply to which entity pairs)
CREATE TABLE IF NOT EXISTS relation_type_applicability (
    id BIGINT PRIMARY KEY,
    relation_type_id BIGINT NOT NULL,
    source_entity_type SMALLINT NOT NULL,
    target_entity_type SMALLINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_rta_relation_type
        FOREIGN KEY (relation_type_id)
        REFERENCES relation_type(id) ON DELETE CASCADE,
    CONSTRAINT rta_UK
        UNIQUE (relation_type_id, source_entity_type, target_entity_type)
);

-- Create sequence for relation_type_applicability
CREATE SEQUENCE IF NOT EXISTS relation_type_applicability_seq INCREMENT BY 10 START WITH 1;

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_rta_relation_type_id ON relation_type_applicability(relation_type_id);
CREATE INDEX IF NOT EXISTS idx_rta_entity_types ON relation_type_applicability(source_entity_type, target_entity_type);
