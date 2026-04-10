-- Add relation_type_id column to artist_track
ALTER TABLE artist_track ADD COLUMN IF NOT EXISTS relation_type_id BIGINT;

-- Add FK constraint to relation_type
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints
        WHERE table_name = 'artist_track'
          AND constraint_name = 'fk_artist_track_relation_type'
    ) THEN
        ALTER TABLE artist_track ADD CONSTRAINT fk_artist_track_relation_type
            FOREIGN KEY (relation_type_id) REFERENCES relation_type(id);
    END IF;
END $$;

-- Drop old unique constraint
ALTER TABLE artist_track DROP CONSTRAINT IF EXISTS uk_artist_track;

-- Create partial unique indexes (handles NULL correctly in PostgreSQL)
CREATE UNIQUE INDEX IF NOT EXISTS uk_artist_track_untyped
    ON artist_track(artist_id, track_id)
    WHERE relation_type_id IS NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uk_artist_track_typed
    ON artist_track(artist_id, track_id, relation_type_id)
    WHERE relation_type_id IS NOT NULL;

-- Index on relation_type_id for lookups
CREATE INDEX IF NOT EXISTS idx_artist_track_relation_type ON artist_track(relation_type_id);
