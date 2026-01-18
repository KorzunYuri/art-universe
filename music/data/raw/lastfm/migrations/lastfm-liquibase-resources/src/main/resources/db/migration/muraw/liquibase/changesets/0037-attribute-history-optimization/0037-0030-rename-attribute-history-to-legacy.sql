ALTER TABLE mu_raw_lastfm.attribute_history
    RENAME TO attribute_history_legacy;

-- Rename the sequence
ALTER SEQUENCE mu_raw_lastfm.attribute_history_seq
    RENAME TO attribute_history_legacy_seq;

-- Rename constraints
ALTER TABLE mu_raw_lastfm.attribute_history_legacy
    RENAME CONSTRAINT attribute_history_pk TO attribute_history_legacy_pk;

ALTER TABLE mu_raw_lastfm.attribute_history_legacy
    RENAME CONSTRAINT "attribute_history$api_call_id_fk" TO "attribute_history_legacy_fk_api_call_id_fk";

ALTER TABLE mu_raw_lastfm.attribute_history_legacy
    RENAME CONSTRAINT "attribute_history$unique_value_valid_till" TO "attribute_history_legacy_fk_unique_value_valid_till";

ALTER TABLE mu_raw_lastfm.attribute_history_legacy
    RENAME CONSTRAINT "attribute_history_fk_attribute_id" TO "attribute_history_legacy_fk_attribute_id";

-- Rename indexes
ALTER INDEX mu_raw_lastfm."attribute_history_i_entity"
    RENAME TO "attribute_history_legacy_i_entity";

ALTER INDEX mu_raw_lastfm."attribute_history_i_entity_type"
    RENAME TO "attribute_history_legacy_i_entity_type";

ALTER INDEX mu_raw_lastfm."attribute_history_i_entity_type_value_current"
    RENAME TO "attribute_history_legacy_i_entity_type_value_current";

ALTER INDEX mu_raw_lastfm."attribute_history_i_entity_value_current"
    RENAME TO "attribute_history_legacy_i_entity_value_current";

ALTER INDEX mu_raw_lastfm."attribute_history_i_scope_entity"
    RENAME TO "attribute_history_legacy_i_scope_entity";

COMMENT ON TABLE mu_raw_lastfm.attribute_history_legacy IS
    'Legacy attribute_history table. To be dropped after data migration is verified.';
