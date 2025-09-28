-- Create composite indexes for entity cleanup operations (mtnc_cleanup_entity function)
-- These partial indexes optimize queries that find entities with low quality metrics and PENDING approval status

-- Album cleanup index (approval_status = 1 AND play_count < threshold)
CREATE INDEX IF NOT EXISTS album_i_cleanup_approval_play_count
ON album (approval_status, play_count) 
WHERE approval_status = 1;

-- Track cleanup index (approval_status = 1 AND play_count < threshold)  
CREATE INDEX IF NOT EXISTS track_i_cleanup_approval_play_count
ON track (approval_status, play_count) 
WHERE approval_status = 1;

-- Tag cleanup index (approval_status = 1 AND usage_count < threshold)
CREATE INDEX IF NOT EXISTS tag_i_cleanup_approval_usage_count
ON tag (approval_status, usage_count) 
WHERE approval_status = 1;

-- Artist cleanup index (approval_status = 1 AND listeners_count < threshold)
-- Note: This complements the existing artist_i_primary_approval_listeners index
CREATE INDEX IF NOT EXISTS artist_i_cleanup_approval_listeners_count
ON artist (approval_status, listeners_count) 
WHERE approval_status = 1;

-- Add comments to explain the purpose
COMMENT ON INDEX album_i_cleanup_approval_play_count IS 'Optimizes mtnc_cleanup_entity function for album deletion by quality thresholds';
COMMENT ON INDEX track_i_cleanup_approval_play_count IS 'Optimizes mtnc_cleanup_entity function for track deletion by quality thresholds';
COMMENT ON INDEX tag_i_cleanup_approval_usage_count IS 'Optimizes mtnc_cleanup_entity function for tag deletion by quality thresholds';
COMMENT ON INDEX artist_i_cleanup_approval_listeners_count IS 'Optimizes mtnc_cleanup_entity function for artist deletion by quality thresholds';
