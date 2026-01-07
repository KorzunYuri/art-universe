-- Create unique indexes for staging tables to support ON CONFLICT deduplication

CREATE UNIQUE INDEX IF NOT EXISTS idx_stg_attribute_history_a_unique 
ON mu_raw_lastfm_staging.stg_attribute_history_a 
(entity_type, entity_id, attribute_id, COALESCE(scope_entity_type, -1), COALESCE(scope_entity_id, -1));

CREATE UNIQUE INDEX IF NOT EXISTS idx_stg_attribute_history_b_unique 
ON mu_raw_lastfm_staging.stg_attribute_history_b 
(entity_type, entity_id, attribute_id, COALESCE(scope_entity_type, -1), COALESCE(scope_entity_id, -1));
