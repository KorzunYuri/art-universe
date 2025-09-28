ALTER TABLE data_snapshot ALTER COLUMN entity_id DROP NOT NULL;
UPDATE data_snapshot SET entity_id = NULL where entity_id = 0;