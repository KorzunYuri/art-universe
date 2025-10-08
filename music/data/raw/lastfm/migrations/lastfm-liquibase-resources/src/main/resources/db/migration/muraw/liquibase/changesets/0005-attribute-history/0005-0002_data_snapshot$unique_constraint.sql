ALTER TABLE data_snapshot
ADD CONSTRAINT data_snapshot$unique_target
    UNIQUE (api_call_type, data_date, entity_type, entity_id);