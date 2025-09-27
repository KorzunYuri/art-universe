-- Create temporary indexes to speed up the migration

CREATE INDEX IF NOT EXISTS tmp_entity_relation_i_scope_entity_type_entity_type
    ON entity_relation (scope_entity_type, entity_type);

CREATE INDEX IF NOT EXISTS tmp_attribute_history_i_current_values
    ON attribute_history (entity_type, entity_id, scope_entity_type, scope_entity_id, attribute_id)
    WHERE valid_till = '9999-12-31'::date;