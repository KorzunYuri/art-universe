-- Add indexes for enhanced track deduplication support

-- Index for MBID-based track search
CREATE INDEX IF NOT EXISTS track_I_mbid ON track (mbid);

-- Composite index for name + artist_id search
CREATE INDEX IF NOT EXISTS track_I_name_artist ON track (name, artist_id);
