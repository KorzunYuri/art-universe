-- Add relation_type_id column to artist_track
ALTER TABLE artist_track ADD COLUMN relation_type_id BIGINT;

-- Add FK constraint to relation_type
ALTER TABLE artist_track ADD CONSTRAINT fk_artist_track_relation_type
    FOREIGN KEY (relation_type_id) REFERENCES relation_type(id);

-- Drop old unique constraint
ALTER TABLE artist_track DROP CONSTRAINT uk_artist_track;

-- Create partial unique indexes (handles NULL correctly in PostgreSQL)
CREATE UNIQUE INDEX uk_artist_track_untyped
    ON artist_track(artist_id, track_id)
    WHERE relation_type_id IS NULL;

CREATE UNIQUE INDEX uk_artist_track_typed
    ON artist_track(artist_id, track_id, relation_type_id)
    WHERE relation_type_id IS NOT NULL;

-- Index on relation_type_id for lookups
CREATE INDEX idx_artist_track_relation_type ON artist_track(relation_type_id);
