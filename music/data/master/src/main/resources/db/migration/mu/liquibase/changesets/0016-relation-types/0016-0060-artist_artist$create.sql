-- Create artist_artist same-entity junction table
CREATE TABLE artist_artist (
    id BIGINT PRIMARY KEY,
    source_artist_id BIGINT NOT NULL,
    target_artist_id BIGINT NOT NULL,
    relation_type_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_artist_artist_source FOREIGN KEY (source_artist_id) REFERENCES artist(id) ON DELETE CASCADE,
    CONSTRAINT fk_artist_artist_target FOREIGN KEY (target_artist_id) REFERENCES artist(id) ON DELETE CASCADE,
    CONSTRAINT fk_artist_artist_relation_type FOREIGN KEY (relation_type_id) REFERENCES relation_type(id),
    CONSTRAINT chk_artist_artist_no_self CHECK (source_artist_id != target_artist_id)
);

-- Create sequence for artist_artist
CREATE SEQUENCE artist_artist_seq INCREMENT BY 50 START WITH 1;

-- Create indexes
CREATE INDEX idx_artist_artist_source ON artist_artist(source_artist_id);
CREATE INDEX idx_artist_artist_target ON artist_artist(target_artist_id);
CREATE INDEX idx_artist_artist_relation_type ON artist_artist(relation_type_id);

-- Partial unique indexes
CREATE UNIQUE INDEX uk_artist_artist_untyped
    ON artist_artist(source_artist_id, target_artist_id)
    WHERE relation_type_id IS NULL;

CREATE UNIQUE INDEX uk_artist_artist_typed
    ON artist_artist(source_artist_id, target_artist_id, relation_type_id)
    WHERE relation_type_id IS NOT NULL;
