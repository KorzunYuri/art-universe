-- Add unique constraint
ALTER TABLE track ADD CONSTRAINT track_reference_id_unique UNIQUE (reference_id);

-- Update comments
COMMENT ON COLUMN track.reference_id IS 'Reference to id in mu.track table (must be unique)';
