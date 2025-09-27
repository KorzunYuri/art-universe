CREATE INDEX attribute_history_I_entity
    ON attribute_history (entity_type, entity_id, scope_entity_type, scope_entity_id, attribute_id);
