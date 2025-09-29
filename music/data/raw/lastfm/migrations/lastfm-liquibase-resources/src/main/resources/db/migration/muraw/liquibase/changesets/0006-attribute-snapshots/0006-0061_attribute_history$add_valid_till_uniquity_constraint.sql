ALTER TABLE attribute_history
    ADD CONSTRAINT attribute_history$unique_value_valid_till
        UNIQUE (entity_type, entity_id, scope_entity_type, scope_entity_id, attribute_id, valid_till);
