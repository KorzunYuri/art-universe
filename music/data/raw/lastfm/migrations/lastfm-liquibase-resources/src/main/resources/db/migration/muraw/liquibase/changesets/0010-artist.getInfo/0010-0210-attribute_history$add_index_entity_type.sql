CREATE INDEX attribute_history_I_entity_type
    ON attribute_history (scope_entity_type, scope_entity_id, entity_type, attribute_id);