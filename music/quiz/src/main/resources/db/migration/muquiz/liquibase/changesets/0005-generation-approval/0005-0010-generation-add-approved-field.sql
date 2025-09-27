-- Add approved field to generation table
ALTER TABLE generation ADD COLUMN approved BOOLEAN NOT NULL DEFAULT FALSE;

COMMENT ON COLUMN generation.approved IS 'Whether this generation is approved for use in quiz';
