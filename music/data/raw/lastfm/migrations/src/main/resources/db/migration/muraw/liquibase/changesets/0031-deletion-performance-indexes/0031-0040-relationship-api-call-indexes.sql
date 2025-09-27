-- Create indexes on api_call_id for relationship tables to optimize FK lookups during deletions
-- These indexes will speed up finding dependent relationship records when deleting api_call records

-- Relationship tables
CREATE INDEX IF NOT EXISTS artist_tag_i_api_call_id ON artist_tag (api_call_id);
CREATE INDEX IF NOT EXISTS artist_track_i_api_call_id ON artist_track (api_call_id);
CREATE INDEX IF NOT EXISTS artist_album_i_api_call_id ON artist_album (api_call_id);
CREATE INDEX IF NOT EXISTS artist_artist_i_api_call_id ON artist_artist (api_call_id);
CREATE INDEX IF NOT EXISTS album_tag_i_api_call_id ON album_tag (api_call_id);
CREATE INDEX IF NOT EXISTS album_track_i_api_call_id ON album_track (api_call_id);
CREATE INDEX IF NOT EXISTS track_tag_i_api_call_id ON track_tag (api_call_id);

-- Add comments to explain the purpose
COMMENT ON INDEX artist_tag_i_api_call_id IS 'Optimizes FK lookups when deleting api_call records';
COMMENT ON INDEX artist_track_i_api_call_id IS 'Optimizes FK lookups when deleting api_call records';
COMMENT ON INDEX artist_album_i_api_call_id IS 'Optimizes FK lookups when deleting api_call records';
COMMENT ON INDEX artist_artist_i_api_call_id IS 'Optimizes FK lookups when deleting api_call records';
COMMENT ON INDEX album_tag_i_api_call_id IS 'Optimizes FK lookups when deleting api_call records';
COMMENT ON INDEX album_track_i_api_call_id IS 'Optimizes FK lookups when deleting api_call records';
COMMENT ON INDEX track_tag_i_api_call_id IS 'Optimizes FK lookups when deleting api_call records';
