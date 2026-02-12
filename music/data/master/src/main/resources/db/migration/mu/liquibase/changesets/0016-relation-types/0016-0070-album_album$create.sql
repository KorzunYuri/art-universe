-- Create album_album same-entity junction table
CREATE TABLE album_album (
    id BIGINT PRIMARY KEY,
    source_album_id BIGINT NOT NULL,
    target_album_id BIGINT NOT NULL,
    relation_type_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_album_album_source FOREIGN KEY (source_album_id) REFERENCES album(id) ON DELETE CASCADE,
    CONSTRAINT fk_album_album_target FOREIGN KEY (target_album_id) REFERENCES album(id) ON DELETE CASCADE,
    CONSTRAINT fk_album_album_relation_type FOREIGN KEY (relation_type_id) REFERENCES relation_type(id),
    CONSTRAINT chk_album_album_no_self CHECK (source_album_id != target_album_id)
);

-- Create sequence for album_album
CREATE SEQUENCE album_album_seq INCREMENT BY 50 START WITH 1;

-- Create indexes
CREATE INDEX idx_album_album_source ON album_album(source_album_id);
CREATE INDEX idx_album_album_target ON album_album(target_album_id);
CREATE INDEX idx_album_album_relation_type ON album_album(relation_type_id);

-- Partial unique indexes
CREATE UNIQUE INDEX uk_album_album_untyped
    ON album_album(source_album_id, target_album_id)
    WHERE relation_type_id IS NULL;

CREATE UNIQUE INDEX uk_album_album_typed
    ON album_album(source_album_id, target_album_id, relation_type_id)
    WHERE relation_type_id IS NOT NULL;
