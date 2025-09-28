CREATE INDEX attribute_history_I_entity_type_value_current
    ON attribute_history (scope_entity_type, scope_entity_id, entity_type, attribute_id)
WHERE valid_till = '9999-12-31';