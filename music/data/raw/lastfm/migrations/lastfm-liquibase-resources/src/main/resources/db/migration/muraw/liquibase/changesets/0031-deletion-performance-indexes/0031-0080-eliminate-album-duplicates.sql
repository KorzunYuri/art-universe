-- Eliminate album duplicates by merging data and removing duplicates
-- This addresses duplicates created by artist name case variations and different API sources

-- Create temporary table with album duplicates analysis
CREATE TEMP TABLE album_duplicates_analysis AS
WITH album_duplicates AS (
    SELECT 
        al.name as album_name,
        a.name as artist_name,
        COUNT(*) as duplicate_count,
        ARRAY_AGG(al.id ORDER BY 
            -- Priority: albums with MBID first, then with play_count, then oldest
            CASE WHEN al.mbid IS NOT NULL AND al.mbid != '' THEN 0 ELSE 1 END,
            CASE WHEN al.play_count IS NOT NULL THEN 0 ELSE 1 END,
            al.created_at ASC
        ) as album_ids
    FROM album al
    JOIN artist a ON al.artist_id = a.id
    WHERE al.name IS NOT NULL AND a.name IS NOT NULL
    GROUP BY al.name, a.name
    HAVING COUNT(*) > 1
)
SELECT 
    album_name,
    artist_name,
    duplicate_count,
    album_ids[1] as keep_album_id,
    album_ids[2:array_length(album_ids, 1)] as delete_album_ids
FROM album_duplicates;

-- Create table for merging album data
CREATE TEMP TABLE album_merge_data AS
SELECT 
    ada.keep_album_id,
    ada.album_name,
    ada.artist_name,
    -- Merge data from all duplicates, prioritizing non-null values
    COALESCE(
        (SELECT al.mbid FROM album al WHERE al.id = ada.keep_album_id AND al.mbid IS NOT NULL AND al.mbid != ''),
        (SELECT al.mbid FROM album al WHERE al.id = ANY(ada.delete_album_ids) AND al.mbid IS NOT NULL AND al.mbid != '' LIMIT 1)
    ) as merged_mbid,
    COALESCE(
        (SELECT al.listeners_count FROM album al WHERE al.id = ada.keep_album_id AND al.listeners_count IS NOT NULL),
        (SELECT al.listeners_count FROM album al WHERE al.id = ANY(ada.delete_album_ids) AND al.listeners_count IS NOT NULL LIMIT 1)
    ) as merged_listeners_count,
    COALESCE(
        (SELECT al.play_count FROM album al WHERE al.id = ada.keep_album_id AND al.play_count IS NOT NULL),
        (SELECT al.play_count FROM album al WHERE al.id = ANY(ada.delete_album_ids) AND al.play_count IS NOT NULL LIMIT 1)
    ) as merged_play_count,
    -- Choose the best URL: keep the primary album's URL (albums don't have normalization issues like tracks)
    (SELECT al.url FROM album al WHERE al.id = ada.keep_album_id) as merged_url
FROM album_duplicates_analysis ada;

-- Update main albums with merged data
UPDATE album 
SET 
    mbid = amd.merged_mbid,
    listeners_count = amd.merged_listeners_count,
    play_count = amd.merged_play_count,
    updated_at = NOW()
FROM album_merge_data amd
WHERE album.id = amd.keep_album_id
AND (
    (album.mbid IS NULL OR album.mbid = '') AND amd.merged_mbid IS NOT NULL OR
    album.listeners_count IS NULL AND amd.merged_listeners_count IS NOT NULL OR
    album.play_count IS NULL AND amd.merged_play_count IS NOT NULL
);

-- Create table for relationship updates
CREATE TEMP TABLE album_relationships_to_update AS
SELECT 
    ada.keep_album_id,
    UNNEST(ada.delete_album_ids) as delete_album_id
FROM album_duplicates_analysis ada;

-- Transfer artist_album relationships (if not exists)
INSERT INTO artist_album (artist_id, album_id, api_call_id, created_at, updated_at)
SELECT DISTINCT 
    aa.artist_id,
    artu.keep_album_id,
    aa.api_call_id,
    NOW(),
    NOW()
FROM album_relationships_to_update artu
JOIN artist_album aa ON aa.album_id = artu.delete_album_id
WHERE NOT EXISTS (
    SELECT 1 FROM artist_album aa2 
    WHERE aa2.artist_id = aa.artist_id 
    AND aa2.album_id = artu.keep_album_id
);

-- Transfer album_track relationships (if not exists)
INSERT INTO album_track (album_id, track_id, api_call_id, position, created_at, updated_at)
SELECT DISTINCT 
    artu.keep_album_id,
    alt.track_id,
    alt.api_call_id,
    alt.position,
    NOW(),
    NOW()
FROM album_relationships_to_update artu
JOIN album_track alt ON alt.album_id = artu.delete_album_id
WHERE NOT EXISTS (
    SELECT 1 FROM album_track alt2 
    WHERE alt2.album_id = artu.keep_album_id 
    AND alt2.track_id = alt.track_id
);

-- Transfer album_tag relationships (if not exists)
INSERT INTO album_tag (album_id, tag_id, api_call_id, usage_count, created_at, updated_at)
SELECT DISTINCT 
    artu.keep_album_id,
    alt.tag_id,
    alt.api_call_id,
    alt.usage_count,
    NOW(),
    NOW()
FROM album_relationships_to_update artu
JOIN album_tag alt ON alt.album_id = artu.delete_album_id
WHERE NOT EXISTS (
    SELECT 1 FROM album_tag alt2 
    WHERE alt2.album_id = artu.keep_album_id 
    AND alt2.tag_id = alt.tag_id
);

-- Delete relationships of duplicate albums
DELETE FROM artist_album WHERE album_id IN (
    SELECT UNNEST(delete_album_ids) FROM album_duplicates_analysis
);

DELETE FROM album_track WHERE album_id IN (
    SELECT UNNEST(delete_album_ids) FROM album_duplicates_analysis
);

DELETE FROM album_tag WHERE album_id IN (
    SELECT UNNEST(delete_album_ids) FROM album_duplicates_analysis
);

-- Delete duplicate albums
DELETE FROM album WHERE id IN (
    SELECT UNNEST(delete_album_ids) FROM album_duplicates_analysis
);

-- Log results
DO $$
DECLARE
    albums_removed INTEGER;
    duplicate_groups INTEGER;
BEGIN
    SELECT COUNT(*) INTO albums_removed FROM (
        SELECT UNNEST(delete_album_ids) FROM album_duplicates_analysis
    ) a;
    
    SELECT COUNT(*) INTO duplicate_groups FROM album_duplicates_analysis;
    
    RAISE NOTICE 'Eliminated % duplicate albums from % groups', albums_removed, duplicate_groups;
END;
$$;
