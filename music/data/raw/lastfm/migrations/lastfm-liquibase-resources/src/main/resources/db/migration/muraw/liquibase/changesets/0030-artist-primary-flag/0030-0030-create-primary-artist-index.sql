-- Create indexes to optimize queries filtering by is_primary flag
-- This will improve performance of API call generation queries

-- Index for primary flag itself (for statistics and validation queries)
CREATE INDEX artist_I_is_primary
    ON artist (is_primary);

-- Partial index for primary artists only (most common query pattern)
CREATE INDEX artist_I_primary_approval_listeners
ON artist (approval_status, listeners_count DESC, id) 
WHERE is_primary = TRUE;
