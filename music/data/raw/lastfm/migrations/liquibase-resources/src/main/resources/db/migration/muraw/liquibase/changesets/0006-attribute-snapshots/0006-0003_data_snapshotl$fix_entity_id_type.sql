ALTER TABLE data_snapshot ALTER COLUMN entity_id DROP DEFAULT;
ALTER TABLE data_snapshot ALTER COLUMN entity_id TYPE BIGINT;
DROP SEQUENCE IF EXISTS data_snapshot_entity_id_seq;