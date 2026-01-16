-- Populate attribute_history_archive with historical values from legacy table
-- This migration copies all records where valid_till != '9999-12-31'

INSERT INTO mu_raw_lastfm.attribute_history_archive (
    id,
    entity_type,
    entity_id,
    attribute_id,
    scope_entity_type,
    scope_entity_id,
    string_value,
    numeric_value,
    collection_ts,
    api_call_id,
    valid_from,
    valid_till
)
SELECT
    id,
    entity_type,
    entity_id,
    attribute_id,
    scope_entity_type,
    scope_entity_id,
    string_value,
    numeric_value,
    collection_ts,
    api_call_id,
    valid_from,
    valid_till
FROM mu_raw_lastfm.attribute_history_legacy
WHERE valid_till != '9999-12-31';

-- Log the count
DO $$
DECLARE
    row_count BIGINT;
BEGIN
    SELECT COUNT(*) INTO row_count FROM mu_raw_lastfm.attribute_history_archive;
    RAISE NOTICE 'Migrated % historical records to attribute_history_archive', row_count;
END $$;
