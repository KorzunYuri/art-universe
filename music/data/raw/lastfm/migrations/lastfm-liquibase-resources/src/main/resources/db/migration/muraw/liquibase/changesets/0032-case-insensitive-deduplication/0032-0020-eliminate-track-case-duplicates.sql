-- Eliminate track case-insensitive duplicates by merging data and removing duplicates
-- This addresses duplicates created by different case variations (e.g., "Track" vs "TRACK" vs "track")

-- Create temporary table with track case-insensitive duplicates analysis
CREATE TEMP TABLE track_case_duplicates_analysis AS
WITH track_case_duplicates AS (
    SELECT 
        LOWER(t.name) as lower_track_name,
        t.artist_id,
        a.name as artist_name,
        COUNT(*) as duplicate_count,
        ARRAY_AGG(t.id ORDER BY 
            -- Priority: tracks with MBID first, then with listeners_count, then oldest
            CASE WHEN t.mbid IS NOT NULL AND t.mbid != '' THEN 0 ELSE 1 END,
            CASE WHEN t.listeners_count IS NOT NULL THEN 0 ELSE 1 END,
            t.created_at ASC
        ) as track_ids,
        ARRAY_AGG(t.name ORDER BY 
            CASE WHEN t.mbid IS NOT NULL AND t.mbid != '' THEN 0 ELSE 1 END,
            CASE WHEN t.listeners_count IS NOT NULL THEN 0 ELSE 1 END,
            t.created_at ASC
        ) as track_names
    FROM track t
    JOIN artist a ON t.artist_id = a.id
    WHERE t.name IS NOT NULL AND a.name IS NOT NULL
    GROUP BY LOWER(t.name), t.artist_id, a.name
    HAVING COUNT(*) > 1
)
SELECT 
    lower_track_name,
    artist_id,
    artist_name,
    duplicate_count,
    track_ids[1] as keep_track_id,
    track_names[1] as keep_track_name,
    track_ids[2:array_length(track_ids, 1)] as delete_track_ids,
    track_names[2:array_length(track_names, 1)] as delete_track_names
FROM track_case_duplicates;

-- Create table for merging track data
CREATE TEMP TABLE track_case_merge_data AS
SELECT 
    tcda.keep_track_id,
    tcda.lower_track_name,
    tcda.keep_track_name,
    tcda.artist_name,
    -- Merge data from all duplicates, prioritizing non-null values
    COALESCE(
        (SELECT t.mbid FROM track t WHERE t.id = tcda.keep_track_id AND t.mbid IS NOT NULL AND t.mbid != ''),
        (SELECT t.mbid FROM track t WHERE t.id = ANY(tcda.delete_track_ids) AND t.mbid IS NOT NULL AND t.mbid != '' LIMIT 1)
    ) as merged_mbid,
    COALESCE(
        (SELECT t.listeners_count FROM track t WHERE t.id = tcda.keep_track_id AND t.listeners_count IS NOT NULL),
        (SELECT t.listeners_count FROM track t WHERE t.id = ANY(tcda.delete_track_ids) AND t.listeners_count IS NOT NULL ORDER BY t.listeners_count DESC LIMIT 1)
    ) as merged_listeners_count,
    COALESCE(
        (SELECT t.play_count FROM track t WHERE t.id = tcda.keep_track_id AND t.play_count IS NOT NULL),
        (SELECT t.play_count FROM track t WHERE t.id = ANY(tcda.delete_track_ids) AND t.play_count IS NOT NULL ORDER BY t.play_count DESC LIMIT 1)
    ) as merged_play_count,
    COALESCE(
        (SELECT t.duration FROM track t WHERE t.id = tcda.keep_track_id AND t.duration IS NOT NULL),
        (SELECT t.duration FROM track t WHERE t.id = ANY(tcda.delete_track_ids) AND t.duration IS NOT NULL LIMIT 1)
    ) as merged_duration,
    -- Choose the best URL: prioritize normalized URLs (with /_/) from any track, then keep_track_id URL
    COALESCE(
        (SELECT t.url FROM track t WHERE t.id = tcda.keep_track_id AND t.url LIKE '%/\_%' ESCAPE '\'),
        (SELECT t.url FROM track t WHERE t.id = ANY(tcda.delete_track_ids) AND t.url LIKE '%/\_%' ESCAPE '\' LIMIT 1),
        (SELECT t.url FROM track t WHERE t.id = tcda.keep_track_id)
    ) as merged_url
FROM track_case_duplicates_analysis tcda;

-- Update main tracks with merged data (WITHOUT URL to avoid conflicts during deletion)
UPDATE track 
SET 
    mbid = tcmd.merged_mbid,
    listeners_count = tcmd.merged_listeners_count,
    play_count = tcmd.merged_play_count,
    duration = tcmd.merged_duration,
    updated_at = NOW()
FROM track_case_merge_data tcmd
WHERE track.id = tcmd.keep_track_id
AND (
    (track.mbid IS NULL OR track.mbid = '') AND tcmd.merged_mbid IS NOT NULL OR
    track.listeners_count IS NULL AND tcmd.merged_listeners_count IS NOT NULL OR
    track.play_count IS NULL AND tcmd.merged_play_count IS NOT NULL OR
    track.duration IS NULL AND tcmd.merged_duration IS NOT NULL
);

-- Create table for relationship updates
CREATE TEMP TABLE track_case_relationships_to_update AS
SELECT 
    tcda.keep_track_id,
    UNNEST(tcda.delete_track_ids) as delete_track_id
FROM track_case_duplicates_analysis tcda;

-- Transfer artist_track relationships (if not exists)
INSERT INTO artist_track (artist_id, track_id, api_call_id, created_at, updated_at)
SELECT DISTINCT 
    at.artist_id,
    tcrtu.keep_track_id,
    at.api_call_id,
    NOW(),
    NOW()
FROM track_case_relationships_to_update tcrtu
JOIN artist_track at ON at.track_id = tcrtu.delete_track_id
WHERE NOT EXISTS (
    SELECT 1 FROM artist_track at2 
    WHERE at2.artist_id = at.artist_id 
    AND at2.track_id = tcrtu.keep_track_id
);

-- Transfer album_track relationships (if not exists)
INSERT INTO album_track (album_id, track_id, api_call_id, position, created_at, updated_at)
SELECT DISTINCT 
    alt.album_id,
    tcrtu.keep_track_id,
    alt.api_call_id,
    alt.position,
    NOW(),
    NOW()
FROM track_case_relationships_to_update tcrtu
JOIN album_track alt ON alt.track_id = tcrtu.delete_track_id
WHERE NOT EXISTS (
    SELECT 1 FROM album_track alt2 
    WHERE alt2.album_id = alt.album_id 
    AND alt2.track_id = tcrtu.keep_track_id
);

-- Transfer track_tag relationships (if not exists)
INSERT INTO track_tag (track_id, tag_id, api_call_id, usage_count, created_at, updated_at)
SELECT DISTINCT 
    tcrtu.keep_track_id,
    tt.tag_id,
    tt.api_call_id,
    tt.usage_count,
    NOW(),
    NOW()
FROM track_case_relationships_to_update tcrtu
JOIN track_tag tt ON tt.track_id = tcrtu.delete_track_id
WHERE NOT EXISTS (
    SELECT 1 FROM track_tag tt2 
    WHERE tt2.track_id = tcrtu.keep_track_id 
    AND tt2.tag_id = tt.tag_id
);

-- Update api_call entity references (where entity_type = 3 for TRACK)
UPDATE api_call 
SET entity_id = tcrtu.keep_track_id, updated_at = NOW()
FROM track_case_relationships_to_update tcrtu
WHERE api_call.entity_type = 3 
AND api_call.entity_id = tcrtu.delete_track_id;

-- Delete relationships of duplicate tracks
DELETE FROM artist_track WHERE track_id IN (
    SELECT UNNEST(delete_track_ids) FROM track_case_duplicates_analysis
);

DELETE FROM album_track WHERE track_id IN (
    SELECT UNNEST(delete_track_ids) FROM track_case_duplicates_analysis
);

DELETE FROM track_tag WHERE track_id IN (
    SELECT UNNEST(delete_track_ids) FROM track_case_duplicates_analysis
);

-- Delete duplicate tracks
DELETE FROM track WHERE id IN (
    SELECT UNNEST(delete_track_ids) FROM track_case_duplicates_analysis
);

-- NOW update URLs of remaining tracks (safe because duplicates are deleted)
UPDATE track 
SET 
    url = tcmd.merged_url,
    updated_at = NOW()
FROM track_case_merge_data tcmd
WHERE track.id = tcmd.keep_track_id
AND track.url != tcmd.merged_url;

-- Log results
DO $$
DECLARE
    tracks_removed INTEGER;
    duplicate_groups INTEGER;
    sample_duplicates TEXT;
BEGIN
    SELECT COUNT(*) INTO tracks_removed FROM (
        SELECT UNNEST(delete_track_ids) FROM track_case_duplicates_analysis
    ) t;
    
    SELECT COUNT(*) INTO duplicate_groups FROM track_case_duplicates_analysis;
    
    -- Get sample of duplicates for logging
    SELECT STRING_AGG(
        lower_track_name || ' by ' || artist_name || ' (' || duplicate_count || ' variants: ' || 
        ARRAY_TO_STRING(keep_track_name || delete_track_names, ' | ') || ')', 
        '; '
    ) INTO sample_duplicates
    FROM (
        SELECT * FROM track_case_duplicates_analysis 
        ORDER BY duplicate_count DESC 
        LIMIT 5
    ) sample;
    
    RAISE NOTICE 'Eliminated % duplicate tracks from % case-insensitive groups', tracks_removed, duplicate_groups;
    RAISE NOTICE 'Sample duplicates: %', sample_duplicates;
END;
$$;
