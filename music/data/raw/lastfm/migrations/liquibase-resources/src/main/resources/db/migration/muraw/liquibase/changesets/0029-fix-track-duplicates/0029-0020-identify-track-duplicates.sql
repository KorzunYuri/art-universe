-- Identify track duplicates that will conflict after URL normalization

-- Create temporary table with URL normalization conflicts
CREATE TEMP TABLE track_url_conflicts AS
WITH normalized_urls AS (
    SELECT 
        t.id,
        t.name,
        t.url,
        t.artist_id,
        t.created_at,
        ac.type as api_call_type,
        CASE 
            WHEN t.url ~ 'https://www\.last\.fm/music/[^/]+/[^/]+/[^/]+$' AND t.url NOT LIKE '%/_/%'
            THEN regexp_replace(t.url, '(https://www\.last\.fm/music/[^/]+)/[^/]+(/[^/]+)$', '\1/_\2')
            ELSE t.url
        END as normalized_url
    FROM track t
    JOIN api_call ac ON t.api_call_id = ac.id
),
url_groups AS (
    SELECT 
        normalized_url,
        COUNT(*) as track_count,
        ARRAY_AGG(id ORDER BY 
            CASE WHEN api_call_type = 11 THEN 1 ELSE 0 END, -- album.getInfo tracks last (to be deleted)
            created_at DESC -- newer tracks last (to be deleted)
        ) as track_ids,
        ARRAY_AGG(api_call_type ORDER BY 
            CASE WHEN api_call_type = 11 THEN 1 ELSE 0 END,
            created_at DESC
        ) as api_call_types
    FROM normalized_urls
    GROUP BY normalized_url
    HAVING COUNT(*) > 1
)
SELECT 
    ug.normalized_url,
    ug.track_count,
    -- Keep the first track (oldest non-album.getInfo), mark others for deletion
    ug.track_ids[1] as track_to_keep,
    ug.track_ids[2:array_length(ug.track_ids, 1)] as tracks_to_delete,
    ug.api_call_types
FROM url_groups ug;

-- Create a flat table of tracks to delete for easier processing
CREATE TEMP TABLE track_duplicates AS
SELECT 
    UNNEST(tuc.tracks_to_delete) as duplicate_track_id,
    tuc.track_to_keep as original_track_id,
    tuc.normalized_url,
    t.name as track_name,
    t.url as duplicate_url,
    t.created_at as duplicate_created,
    ac.type as duplicate_api_call_type
FROM track_url_conflicts tuc
CROSS JOIN UNNEST(tuc.tracks_to_delete) as duplicate_id
JOIN track t ON t.id = duplicate_id
JOIN api_call ac ON t.api_call_id = ac.id;

-- Log the conflicts found
DO $$
DECLARE
    conflict_count INTEGER;
    duplicate_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO conflict_count FROM track_url_conflicts;
    SELECT COUNT(*) INTO duplicate_count FROM track_duplicates;
    RAISE NOTICE 'Found % URL conflicts affecting % duplicate tracks', conflict_count, duplicate_count;
END $$;
