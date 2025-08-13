-- Add indexes for tag-based entity search optimization

-- Index for artist_tag table to optimize tag-based artist search
CREATE INDEX IF NOT EXISTS idx_artist_tag_tag_id ON artist_tag (tag_id);

-- Composite index for artist_tag to optimize EXISTS queries
CREATE INDEX IF NOT EXISTS idx_artist_tag_tag_artist ON artist_tag (tag_id, artist_id);

-- Index for track_tag table to optimize tag-based track search
CREATE INDEX IF NOT EXISTS idx_track_tag_tag_id ON track_tag (tag_id);

-- Composite index for track_tag to optimize EXISTS queries
CREATE INDEX IF NOT EXISTS idx_track_tag_tag_track ON track_tag (tag_id, track_id);

-- Index for album_tag table to optimize tag-based album search
CREATE INDEX IF NOT EXISTS idx_album_tag_tag_id ON album_tag (tag_id);

-- Composite index for album_tag to optimize EXISTS queries
CREATE INDEX IF NOT EXISTS idx_album_tag_tag_album ON album_tag (tag_id, album_id);
