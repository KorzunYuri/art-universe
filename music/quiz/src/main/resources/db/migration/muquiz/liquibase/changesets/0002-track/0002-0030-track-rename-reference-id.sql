-- Rename column reference_id to master_id
ALTER TABLE track RENAME COLUMN reference_id TO master_id;

-- Rename index
DROP INDEX idx_track_reference_id;
CREATE INDEX idx_track_master_id ON track(master_id);

-- Rename constraint
ALTER TABLE track DROP CONSTRAINT track_reference_id_unique;
ALTER TABLE track ADD CONSTRAINT track_master_id_unique UNIQUE (master_id);

-- Update comments
COMMENT ON COLUMN track.master_id IS 'Reference to id in mu.track table (must be unique)';
