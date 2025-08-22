-- Eliminate album case-insensitive duplicates by merging data and removing duplicates
-- This addresses duplicates created by different case variations (e.g., "Album" vs "ALBUM" vs "album")

-- Create temporary table with album case-insensitive duplicates analysis
CREATE TEMP TABLE album_case_duplicates_analysis AS
WITH album_case_duplicates AS (
    SELECT 
        LOWER(al.name) as lower_album_name,
        al.artist_id,
        a.name as artist_name,
        COUNT(*) as duplicate_count,
        ARRAY_AGG(al.id ORDER BY 
            -- Priority: albums with MBID first, then with play_count, then oldest
            CASE WHEN al.mbid IS NOT NULL AND al.mbid != '' THEN 0 ELSE 1 END,
            CASE WHEN al.play_count IS NOT NULL THEN 0 ELSE 1 END,
            al.created_at ASC
        ) as album_ids,
        ARRAY_AGG(al.name ORDER BY 
            CASE WHEN al.mbid IS NOT NULL AND al.mbid != '' THEN 0 ELSE 1 END,
            CASE WHEN al.play_count IS NOT NULL THEN 0 ELSE 1 END,
            al.created_at ASC
        ) as album_names
    FROM album al
    JOIN artist a ON al.artist_id = a.id
    WHERE al.name IS NOT NULL AND a.name IS NOT NULL
    GROUP BY LOWER(al.name), al.artist_id, a.name
    HAVING COUNT(*) > 1
)
SELECT 
    lower_album_name,
    artist_id,
    artist_name,
    duplicate_count,
    album_ids[1] as keep_album_id,
    album_names[1] as keep_album_name,
    album_ids[2:array_length(album_ids, 1)] as delete_album_ids,
    album_names[2:array_length(album_names, 1)] as delete_album_names
FROM album_case_duplicates;

-- Create table for merging album data
CREATE TEMP TABLE album_case_merge_data AS
SELECT 
    acda.keep_album_id,
    acda.lower_album_name,
    acda.keep_album_name,
    acda.artist_name,
    -- Merge data from all duplicates, prioritizing non-null values
    COALESCE(
        (SELECT al.mbid FROM album al WHERE al.id = acda.keep_album_id AND al.mbid IS NOT NULL AND al.mbid != ''),
        (SELECT al.mbid FROM album al WHERE al.id = ANY(acda.delete_album_ids) AND al.mbid IS NOT NULL AND al.mbid != '' LIMIT 1)
    ) as merged_mbid,
    COALESCE(
        (SELECT al.listeners_count FROM album al WHERE al.id = acda.keep_album_id AND al.listeners_count IS NOT NULL),
        (SELECT al.listeners_count FROM album al WHERE al.id = ANY(acda.delete_album_ids) AND al.listeners_count IS NOT NULL ORDER BY al.listeners_count DESC LIMIT 1)
    ) as merged_listeners_count,
    COALESCE(
        (SELECT al.play_count FROM album al WHERE al.id = acda.keep_album_id AND al.play_count IS NOT NULL),
        (SELECT al.play_count FROM album al WHERE al.id = ANY(acda.delete_album_ids) AND al.play_count IS NOT NULL ORDER BY al.play_count DESC LIMIT 1)
    ) as merged_play_count,
    -- Keep the URL from the primary album (albums don't have normalization issues like tracks)
    (SELECT al.url FROM album al WHERE al.id = acda.keep_album_id) as merged_url
FROM album_case_duplicates_analysis acda;

-- Update main albums with merged data
UPDATE album 
SET 
    mbid = acmd.merged_mbid,
    listeners_count = acmd.merged_listeners_count,
    play_count = acmd.merged_play_count,
    updated_at = NOW()
FROM album_case_merge_data acmd
WHERE album.id = acmd.keep_album_id
AND (
    (album.mbid IS NULL OR album.mbid = '') AND acmd.merged_mbid IS NOT NULL OR
    album.listeners_count IS NULL AND acmd.merged_listeners_count IS NOT NULL OR
    album.play_count IS NULL AND acmd.merged_play_count IS NOT NULL
);

-- Create table for relationship updates
CREATE TEMP TABLE album_case_relationships_to_update AS
SELECT 
    acda.keep_album_id,
    UNNEST(acda.delete_album_ids) as delete_album_id
FROM album_case_duplicates_analysis acda;

-- Transfer artist_album relationships (if not exists)
INSERT INTO artist_album (artist_id, album_id, api_call_id, created_at, updated_at)
SELECT DISTINCT 
    aa.artist_id,
    acrtu.keep_album_id,
    aa.api_call_id,
    NOW(),
    NOW()
FROM album_case_relationships_to_update acrtu
JOIN artist_album aa ON aa.album_id = acrtu.delete_album_id
WHERE NOT EXISTS (
    SELECT 1 FROM artist_album aa2 
    WHERE aa2.artist_id = aa.artist_id 
    AND aa2.album_id = acrtu.keep_album_id
);

-- Transfer album_track relationships (if not exists)
INSERT INTO album_track (album_id, track_id, api_call_id, position, created_at, updated_at)
SELECT DISTINCT 
    acrtu.keep_album_id,
    alt.track_id,
    alt.api_call_id,
    alt.position,
    NOW(),
    NOW()
FROM album_case_relationships_to_update acrtu
JOIN album_track alt ON alt.album_id = acrtu.delete_album_id
WHERE NOT EXISTS (
    SELECT 1 FROM album_track alt2 
    WHERE alt2.album_id = acrtu.keep_album_id 
    AND alt2.track_id = alt.track_id
);

-- Transfer album_tag relationships (if not exists)
INSERT INTO album_tag (album_id, tag_id, api_call_id, usage_count, created_at, updated_at)
SELECT DISTINCT 
    acrtu.keep_album_id,
    alt.tag_id,
    alt.api_call_id,
    alt.usage_count,
    NOW(),
    NOW()
FROM album_case_relationships_to_update acrtu
JOIN album_tag alt ON alt.album_id = acrtu.delete_album_id
WHERE NOT EXISTS (
    SELECT 1 FROM album_tag alt2 
    WHERE alt2.album_id = acrtu.keep_album_id 
    AND alt2.tag_id = alt.tag_id
);

-- Update api_call entity references (where entity_type = 2 for ALBUM)
UPDATE api_call 
SET entity_id = acrtu.keep_album_id, updated_at = NOW()
FROM album_case_relationships_to_update acrtu
WHERE api_call.entity_type = 2 
AND api_call.entity_id = acrtu.delete_album_id;

-- Delete relationships of duplicate albums
DELETE FROM artist_album WHERE album_id IN (
    SELECT UNNEST(delete_album_ids) FROM album_case_duplicates_analysis
);

DELETE FROM album_track WHERE album_id IN (
    SELECT UNNEST(delete_album_ids) FROM album_case_duplicates_analysis
);

DELETE FROM album_tag WHERE album_id IN (
    SELECT UNNEST(delete_album_ids) FROM album_case_duplicates_analysis
);

-- Delete duplicate albums
DELETE FROM album WHERE id IN (
    SELECT UNNEST(delete_album_ids) FROM album_case_duplicates_analysis
);

-- Log results
DO $$
DECLARE
    albums_removed INTEGER;
    duplicate_groups INTEGER;
    sample_duplicates TEXT;
BEGIN
    SELECT COUNT(*) INTO albums_removed FROM (
        SELECT UNNEST(delete_album_ids) FROM album_case_duplicates_analysis
    ) a;
    
    SELECT COUNT(*) INTO duplicate_groups FROM album_case_duplicates_analysis;
    
    -- Get sample of duplicates for logging
    SELECT STRING_AGG(
        lower_album_name || ' by ' || artist_name || ' (' || duplicate_count || ' variants: ' || 
        ARRAY_TO_STRING(keep_album_name || delete_album_names, ' | ') || ')', 
        '; '
    ) INTO sample_duplicates
    FROM (
        SELECT * FROM album_case_duplicates_analysis 
        ORDER BY duplicate_count DESC 
        LIMIT 5
    ) sample;
    
    RAISE NOTICE 'Eliminated % duplicate albums from % case-insensitive groups', albums_removed, duplicate_groups;
    RAISE NOTICE 'Sample duplicates: %', sample_duplicates;
END $$;
