-- Create table to track deleted entities for music-data module unbinding

CREATE TABLE mtnc_deleted_entities (
    id BIGSERIAL PRIMARY KEY,
    cleanup_run_id BIGINT NOT NULL REFERENCES mtnc_cleanup_run(id),
    entity_type INTEGER NOT NULL,
    entity_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for efficient querying
CREATE INDEX idx_mtnc_deleted_entities_cleanup_run 
    ON mtnc_deleted_entities(cleanup_run_id);

CREATE INDEX idx_mtnc_deleted_entities_type_id 
    ON mtnc_deleted_entities(entity_type, entity_id);

CREATE INDEX idx_mtnc_deleted_entities_created_at 
    ON mtnc_deleted_entities(created_at);

-- Add comment
COMMENT ON TABLE mtnc_deleted_entities IS 
    'Tracks entities deleted during maintenance cleanup for unbinding in music-data module';

COMMENT ON COLUMN mtnc_deleted_entities.entity_type IS 
    'Entity type code: 1=artist, 2=album, 3=track, 4=tag';

COMMENT ON COLUMN mtnc_deleted_entities.entity_id IS 
    'ID of the deleted entity';

COMMENT ON COLUMN mtnc_deleted_entities.cleanup_run_id IS 
    'Reference to the cleanup run that deleted this entity';
