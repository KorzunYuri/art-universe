-- Create track_track same-entity junction table
CREATE TABLE track_track (
    id BIGINT PRIMARY KEY,
    source_track_id BIGINT NOT NULL,
    target_track_id BIGINT NOT NULL,
    relation_type_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_track_track_source FOREIGN KEY (source_track_id) REFERENCES track(id) ON DELETE CASCADE,
    CONSTRAINT fk_track_track_target FOREIGN KEY (target_track_id) REFERENCES track(id) ON DELETE CASCADE,
    CONSTRAINT fk_track_track_relation_type FOREIGN KEY (relation_type_id) REFERENCES relation_type(id),
    CONSTRAINT chk_track_track_no_self CHECK (source_track_id != target_track_id)
);

-- Create sequence for track_track
CREATE SEQUENCE track_track_seq INCREMENT BY 50 START WITH 1;

-- Create indexes
CREATE INDEX idx_track_track_source ON track_track(source_track_id);
CREATE INDEX idx_track_track_target ON track_track(target_track_id);
CREATE INDEX idx_track_track_relation_type ON track_track(relation_type_id);

-- Partial unique indexes
CREATE UNIQUE INDEX uk_track_track_untyped
    ON track_track(source_track_id, target_track_id)
    WHERE relation_type_id IS NULL;

CREATE UNIQUE INDEX uk_track_track_typed
    ON track_track(source_track_id, target_track_id, relation_type_id)
    WHERE relation_type_id IS NOT NULL;
