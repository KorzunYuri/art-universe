-- Create indexes on api_call_id for main entity tables to optimize FK lookups during deletions
-- These indexes will speed up finding dependent records when deleting api_call records

-- Main entity tables
CREATE INDEX IF NOT EXISTS artist_i_api_call_id ON artist (api_call_id);
CREATE INDEX IF NOT EXISTS album_i_api_call_id ON album (api_call_id);
CREATE INDEX IF NOT EXISTS track_i_api_call_id ON track (api_call_id);
CREATE INDEX IF NOT EXISTS tag_i_api_call_id ON tag (api_call_id);

-- Add comments to explain the purpose
COMMENT ON INDEX artist_i_api_call_id IS 'Optimizes FK lookups when deleting api_call records';
COMMENT ON INDEX album_i_api_call_id IS 'Optimizes FK lookups when deleting api_call records';
COMMENT ON INDEX track_i_api_call_id IS 'Optimizes FK lookups when deleting api_call records';
COMMENT ON INDEX tag_i_api_call_id IS 'Optimizes FK lookups when deleting api_call records';
