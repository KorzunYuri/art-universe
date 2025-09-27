ALTER TABLE api_call ALTER COLUMN entity_id DROP NOT NULL;
UPDATE api_call SET entity_id = NULL where entity_id = 0;