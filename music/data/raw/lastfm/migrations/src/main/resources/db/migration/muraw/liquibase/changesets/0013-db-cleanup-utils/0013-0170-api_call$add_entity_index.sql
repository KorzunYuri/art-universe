DROP INDEX IF EXISTS api_call_I_entity;
CREATE INDEX IF NOT EXISTS api_call_I_entity ON api_call (entity_type, entity_id, status);