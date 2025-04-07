ALTER TABLE artist
    ALTER COLUMN is_streamable SET DEFAULT FALSE;
UPDATE artist SET is_streamable = FALSE WHERE is_streamable IS NULL;