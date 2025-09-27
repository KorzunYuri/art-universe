-- Rename column reference_id to master_id
ALTER TABLE artist RENAME COLUMN reference_id TO master_id;

-- Rename index
DROP INDEX idx_artist_reference_id;
CREATE INDEX idx_artist_master_id ON artist(master_id);

-- Rename constraint
ALTER TABLE artist DROP CONSTRAINT artist_reference_id_unique;
ALTER TABLE artist ADD CONSTRAINT artist_master_id_unique UNIQUE (master_id);

-- Update comments
COMMENT ON COLUMN artist.master_id IS 'Reference to id in mu.artist table (must be unique)';
