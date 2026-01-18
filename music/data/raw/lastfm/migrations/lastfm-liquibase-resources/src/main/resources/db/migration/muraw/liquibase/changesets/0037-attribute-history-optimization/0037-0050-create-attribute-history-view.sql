-- Create a union view to maintain backward compatibility for read operations
-- This view combines current values with historical values
CREATE OR REPLACE VIEW mu_raw_lastfm.attribute_history AS
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
    '9999-12-31'::date AS valid_till
FROM mu_raw_lastfm.attribute_history_current

UNION ALL

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
FROM mu_raw_lastfm.attribute_history_archive;

COMMENT ON VIEW mu_raw_lastfm.attribute_history IS
    'Union view combining current and historical attribute values. Use attribute_history_current for writes.';
