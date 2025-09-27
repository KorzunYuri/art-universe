-- Normalize remaining track URLs after duplicate elimination
-- This fixes track URLs that still have album information in them
-- Safe to run after duplicates are eliminated to avoid unique constraint violations

-- Normalize track URLs by replacing /AlbumName/ with /_/
-- Use proper escaping for the underscore character in LIKE pattern
UPDATE track 
SET 
    url = regexp_replace(
        url, 
        '(https://www\.last\.fm/music/[^/]+)/[^/]+(/[^/]+)$', 
        '\1/_\2'
    ),
    updated_at = NOW()
WHERE url ~ 'https://www\.last\.fm/music/[^/]+/[^/]+/[^/]+$'
AND url NOT LIKE '%/\_%' ESCAPE '\';

-- Log the number of URLs that were normalized
DO $$
DECLARE
    normalized_count INTEGER;
BEGIN
    GET DIAGNOSTICS normalized_count = ROW_COUNT;
    RAISE NOTICE 'Normalized % track URLs', normalized_count;
END $$;
