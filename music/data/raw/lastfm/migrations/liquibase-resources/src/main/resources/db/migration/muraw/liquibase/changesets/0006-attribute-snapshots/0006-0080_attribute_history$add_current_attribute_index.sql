CREATE INDEX attribute_history_I_current_value
    ON attribute_history (entity_type, entity_id, scope_entity_type, scope_entity_id, attribute_id)
WHERE valid_till = '9999-12-31';
