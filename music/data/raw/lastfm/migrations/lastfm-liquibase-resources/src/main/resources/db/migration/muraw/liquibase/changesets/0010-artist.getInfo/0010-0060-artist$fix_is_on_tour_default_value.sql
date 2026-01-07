ALTER TABLE artist
    ALTER COLUMN is_on_tour SET DEFAULT FALSE;
UPDATE artist SET is_on_tour = FALSE WHERE is_on_tour IS NULL;