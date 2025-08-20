-- Normalize track URLs by removing album information

-- Update track URLs, replacing /AlbumName/ with /_/
UPDATE track 
SET url = regexp_replace(
    url, 
    '(https://www\.last\.fm/music/[^/]+)/[^/]+(/[^/]+)$', 
    '\1/_\2'
)
WHERE url ~ 'https://www\.last\.fm/music/[^/]+/[^/]+/[^/]+$'
AND url NOT LIKE '%/_/%';
