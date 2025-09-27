-- Create additional performance indexes for deletion operations

-- Index for finding orphaned api_call records (entity_type IS NOT NULL AND entity_id IS NOT NULL)
-- This optimizes queries to find api_call records that reference specific entities
CREATE INDEX IF NOT EXISTS api_call_i_entity_lookup
ON api_call (entity_type, entity_id) 
WHERE entity_type IS NOT NULL AND entity_id IS NOT NULL;

-- Index for listeners_count on track and album tables (for quality-based filtering)
CREATE INDEX IF NOT EXISTS track_i_listeners_count ON track (listeners_count);
CREATE INDEX IF NOT EXISTS album_i_listeners_count ON album (listeners_count);

-- Add comments to explain the purpose
COMMENT ON INDEX api_call_i_entity_lookup IS 'Optimizes finding api_call records that reference specific entities';
COMMENT ON INDEX track_i_listeners_count IS 'Optimizes quality-based filtering and sorting of tracks';
COMMENT ON INDEX album_i_listeners_count IS 'Optimizes quality-based filtering and sorting of albums';
