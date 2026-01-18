-- Populate attribute_history_current with current values from legacy table
-- This migration copies all records where valid_till = '9999-12-31'

INSERT INTO mu_raw_lastfm.attribute_history_current (
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
    valid_from
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
    valid_from
FROM mu_raw_lastfm.attribute_history_legacy
WHERE valid_till = '9999-12-31';

-- Log the count
DO $$
DECLARE
    row_count BIGINT;
BEGIN
    SELECT COUNT(*) INTO row_count FROM mu_raw_lastfm.attribute_history_current;
    RAISE NOTICE 'Migrated % current records to attribute_history_current', row_count;
END $$;
