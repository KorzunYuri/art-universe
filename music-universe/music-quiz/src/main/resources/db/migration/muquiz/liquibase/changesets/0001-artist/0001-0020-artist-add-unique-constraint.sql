-- Add unique constraint
ALTER TABLE artist ADD CONSTRAINT artist_reference_id_unique UNIQUE (reference_id);

-- Update comments
COMMENT ON COLUMN artist.reference_id IS 'Reference to id in mu.artist table (must be unique)';
