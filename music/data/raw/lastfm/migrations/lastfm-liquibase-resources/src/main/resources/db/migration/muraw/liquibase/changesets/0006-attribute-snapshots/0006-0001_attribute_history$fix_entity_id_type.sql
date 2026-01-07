ALTER TABLE attribute_history ALTER COLUMN entity_id DROP DEFAULT;
ALTER TABLE attribute_history ALTER COLUMN entity_id TYPE BIGINT;
DROP SEQUENCE IF EXISTS attribute_history_entity_id_seq;