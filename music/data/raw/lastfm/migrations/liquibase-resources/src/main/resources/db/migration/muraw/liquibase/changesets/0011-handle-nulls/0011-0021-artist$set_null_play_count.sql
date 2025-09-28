ALTER TABLE artist ALTER COLUMN play_count DROP DEFAULT;
UPDATE artist SET play_count = NULL where play_count < 1;