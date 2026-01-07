ALTER TABLE artist ALTER COLUMN listeners_count DROP DEFAULT;
UPDATE artist SET listeners_count = NULL where listeners_count < 1;