-- Eliminate artist case-insensitive duplicates by merging data and removing duplicates
-- This addresses duplicates created by different case variations (e.g., "Artist" vs "ARTIST" vs "artist")

-- Create temporary table with artist case-insensitive duplicates analysis
CREATE TEMP TABLE artist_case_duplicates_analysis AS
WITH artist_case_duplicates AS (
    SELECT
        LOWER(name) as lower_name,
        COUNT(*) as duplicate_count,
        ARRAY_AGG(id ORDER BY
            -- Priority: artists with MBID first, then with listeners_count, then with approval_status (APPROVED=2), then oldest
            CASE WHEN mbid IS NOT NULL AND mbid != '' THEN 0 ELSE 1 END,
            CASE WHEN listeners_count IS NOT NULL THEN 0 ELSE 1 END,
            CASE WHEN approval_status = 2 THEN 0 ELSE 1 END,
            created_at ASC
        ) as artist_ids,
        ARRAY_AGG(name ORDER BY
            CASE WHEN mbid IS NOT NULL AND mbid != '' THEN 0 ELSE 1 END,
            CASE WHEN listeners_count IS NOT NULL THEN 0 ELSE 1 END,
            CASE WHEN approval_status = 2 THEN 0 ELSE 1 END,
            created_at ASC
        ) as artist_names
    FROM artist
    WHERE name IS NOT NULL
    GROUP BY LOWER(name)
    HAVING COUNT(*) > 1
)
SELECT
    lower_name,
    duplicate_count,
    artist_ids[1] as keep_artist_id,
    artist_names[1] as keep_artist_name,
    artist_ids[2:array_length(artist_ids, 1)] as delete_artist_ids,
    artist_names[2:array_length(artist_names, 1)] as delete_artist_names
FROM artist_case_duplicates;

-- Create table for merging artist data
CREATE TEMP TABLE artist_case_merge_data AS
SELECT
    acda.keep_artist_id,
    acda.lower_name,
    acda.keep_artist_name,
    -- Merge data from all duplicates, prioritizing non-null values
    COALESCE(
            (SELECT a.mbid FROM artist a WHERE a.id = acda.keep_artist_id AND a.mbid IS NOT NULL AND a.mbid != ''),
            (SELECT a.mbid FROM artist a WHERE a.id = ANY(acda.delete_artist_ids) AND a.mbid IS NOT NULL AND a.mbid != '' LIMIT 1)
    ) as merged_mbid,
    COALESCE(
            (SELECT a.listeners_count FROM artist a WHERE a.id = acda.keep_artist_id AND a.listeners_count IS NOT NULL),
            (SELECT a.listeners_count FROM artist a WHERE a.id = ANY(acda.delete_artist_ids) AND a.listeners_count IS NOT NULL ORDER BY a.listeners_count DESC LIMIT 1)
    ) as merged_listeners_count,
    COALESCE(
            (SELECT a.play_count FROM artist a WHERE a.id = acda.keep_artist_id AND a.play_count IS NOT NULL),
            (SELECT a.play_count FROM artist a WHERE a.id = ANY(acda.delete_artist_ids) AND a.play_count IS NOT NULL ORDER BY a.play_count DESC LIMIT 1)
    ) as merged_play_count,
    -- Keep the URL from the primary artist (URLs are usually consistent for same artist)
    (SELECT a.url FROM artist a WHERE a.id = acda.keep_artist_id) as merged_url,
    -- Keep the highest approval status
    GREATEST(
            (SELECT a.approval_status FROM artist a WHERE a.id = acda.keep_artist_id),
            COALESCE((SELECT MAX(a.approval_status) FROM artist a WHERE a.id = ANY(acda.delete_artist_ids)), 1)
    ) as merged_approval_status
FROM artist_case_duplicates_analysis acda;

-- Update main artists with merged data
UPDATE artist
SET
    mbid = acmd.merged_mbid,
    listeners_count = acmd.merged_listeners_count,
    play_count = acmd.merged_play_count,
    approval_status = acmd.merged_approval_status,
    updated_at = NOW()
FROM artist_case_merge_data acmd
WHERE artist.id = acmd.keep_artist_id
  AND (
    (artist.mbid IS NULL OR artist.mbid = '') AND acmd.merged_mbid IS NOT NULL OR
    artist.listeners_count IS NULL AND acmd.merged_listeners_count IS NOT NULL OR
    artist.play_count IS NULL AND acmd.merged_play_count IS NOT NULL OR
    artist.approval_status < acmd.merged_approval_status
    );

-- Create table for relationship updates
CREATE TEMP TABLE artist_case_relationships_to_update AS
SELECT
    acda.keep_artist_id,
    UNNEST(acda.delete_artist_ids) as delete_artist_id
FROM artist_case_duplicates_analysis acda;

-- Transfer artist_tag relationships (if not exists)
INSERT INTO artist_tag (artist_id, tag_id, api_call_id, usage_count, created_at, updated_at)
SELECT DISTINCT
    acrtu.keep_artist_id,
    at.tag_id,
    MIN(at.api_call_id) as api_call_id,  -- Use MIN to pick one api_call_id from duplicates
    MAX(at.usage_count) as usage_count,  -- Use MAX to handle multiple same relations
    NOW(),
    NOW()
FROM artist_case_relationships_to_update acrtu
         JOIN artist_tag at ON at.artist_id = acrtu.delete_artist_id
WHERE NOT EXISTS (
    SELECT 1 FROM artist_tag at2
    WHERE at2.artist_id = acrtu.keep_artist_id
      AND at2.tag_id = at.tag_id
)
GROUP BY acrtu.keep_artist_id, at.tag_id;  -- GROUP BY without api_call_id to deduplicate properly

-- Transfer artist_artist relationships (source) (if not exists)
INSERT INTO artist_artist (source_artist_id, target_artist_id, api_call_id, relation_type, match_score, created_at, updated_at)
SELECT DISTINCT
    acrtu.keep_artist_id,
    aa.target_artist_id,
    MIN(aa.api_call_id) as api_call_id,  -- Use MIN to pick one api_call_id from duplicates
    aa.relation_type,
    MAX(aa.match_score),
    NOW(),
    NOW()
FROM artist_case_relationships_to_update acrtu
         JOIN artist_artist aa ON aa.source_artist_id = acrtu.delete_artist_id
WHERE aa.target_artist_id != acrtu.keep_artist_id  -- Avoid self-references
  AND NOT EXISTS (
    SELECT 1 FROM artist_artist aa2
    WHERE aa2.source_artist_id = acrtu.keep_artist_id
      AND aa2.target_artist_id = aa.target_artist_id
      AND aa2.relation_type = aa.relation_type  -- ADD relation_type to avoid UK violation
)
GROUP BY aa.target_artist_id, acrtu.keep_artist_id, aa.relation_type;

-- Transfer artist_artist relationships (target) (if not exists)
INSERT INTO artist_artist (source_artist_id, target_artist_id, api_call_id, relation_type, match_score, created_at, updated_at)
SELECT DISTINCT
    aa.source_artist_id,
    acrtu.keep_artist_id,
    MIN(aa.api_call_id) as api_call_id,  -- Use MIN to pick one api_call_id from duplicates
    aa.relation_type,
    max(aa.match_score),
    NOW(),
    NOW()
FROM artist_case_relationships_to_update acrtu
         JOIN artist_artist aa ON aa.target_artist_id = acrtu.delete_artist_id
WHERE aa.source_artist_id != acrtu.keep_artist_id  -- Avoid self-references
  AND NOT EXISTS (
    SELECT 1 FROM artist_artist aa2
    WHERE aa2.source_artist_id = aa.source_artist_id
      AND aa2.target_artist_id = acrtu.keep_artist_id
      AND aa2.relation_type = aa.relation_type  -- ADD relation_type to avoid UK violation
)
-- Also check for reverse relationship to avoid bidirectional duplicates
  AND NOT EXISTS (
    SELECT 1 FROM artist_artist aa3
    WHERE aa3.source_artist_id = acrtu.keep_artist_id
      AND aa3.target_artist_id = aa.source_artist_id
      AND aa3.relation_type = aa.relation_type
)
GROUP BY aa.source_artist_id, acrtu.keep_artist_id, aa.relation_type;

-- Transfer artist_album relationships (if not exists)
INSERT INTO artist_album (artist_id, album_id, api_call_id, created_at, updated_at)
SELECT DISTINCT
    acrtu.keep_artist_id,
    aa.album_id,
    MIN(aa.api_call_id) as api_call_id,  -- Use MIN to pick one api_call_id from duplicates
    NOW(),
    NOW()
FROM artist_case_relationships_to_update acrtu
         JOIN artist_album aa ON aa.artist_id = acrtu.delete_artist_id
WHERE NOT EXISTS (
    SELECT 1 FROM artist_album aa2
    WHERE aa2.artist_id = acrtu.keep_artist_id
      AND aa2.album_id = aa.album_id
)
GROUP BY acrtu.keep_artist_id, aa.album_id;  -- GROUP BY without api_call_id to deduplicate properly

-- Transfer artist_track relationships (if not exists)
INSERT INTO artist_track (artist_id, track_id, api_call_id, created_at, updated_at)
SELECT DISTINCT
    acrtu.keep_artist_id,
    at.track_id,
    MIN(at.api_call_id) as api_call_id,  -- Use MIN to pick one api_call_id from duplicates
    NOW(),
    NOW()
FROM artist_case_relationships_to_update acrtu
         JOIN artist_track at ON at.artist_id = acrtu.delete_artist_id
WHERE NOT EXISTS (
    SELECT 1 FROM artist_track at2
    WHERE at2.artist_id = acrtu.keep_artist_id
      AND at2.track_id = at.track_id
)
GROUP BY acrtu.keep_artist_id, at.track_id;  -- GROUP BY without api_call_id to deduplicate properly;

-- Update album.artist_id references (handle conflicts with unique constraint album_u_name_artist_id)
-- First, delete albums that would create conflicts
DELETE FROM album 
WHERE id IN (
    -- Delete albums that conflict with existing albums of the main artist
    SELECT al_dup.id
    FROM album al_dup
    JOIN artist_case_relationships_to_update acrtu ON al_dup.artist_id = acrtu.delete_artist_id
    WHERE EXISTS (
        SELECT 1 FROM album al_existing 
        WHERE al_existing.artist_id = acrtu.keep_artist_id 
        AND al_existing.name = al_dup.name
    )
    
    UNION
    
    -- Delete duplicate albums among those being transferred (keep only one per name+target_artist)
    SELECT al_dup.id
    FROM album al_dup
    JOIN artist_case_relationships_to_update acrtu ON al_dup.artist_id = acrtu.delete_artist_id
    WHERE al_dup.id NOT IN (
        -- Keep the oldest album for each name+target_artist combination
        SELECT MIN(al_inner.id)
        FROM album al_inner
        JOIN artist_case_relationships_to_update acrtu_inner ON al_inner.artist_id = acrtu_inner.delete_artist_id
        WHERE acrtu_inner.keep_artist_id = acrtu.keep_artist_id
          AND al_inner.name = al_dup.name
        GROUP BY acrtu_inner.keep_artist_id, al_inner.name
    )
);

-- Then, update remaining albums (those that won't create conflicts)
UPDATE album
SET artist_id = acrtu.keep_artist_id, updated_at = NOW()
FROM artist_case_relationships_to_update acrtu
WHERE album.artist_id = acrtu.delete_artist_id;

-- Update track.artist_id references (handle conflicts with unique constraint track_u_name_artist_id)
-- First, delete tracks that would create conflicts
DELETE FROM track
WHERE id IN (
    -- Delete tracks that conflict with existing tracks of the main artist
    SELECT t_dup.id
    FROM track t_dup
    JOIN artist_case_relationships_to_update acrtu ON t_dup.artist_id = acrtu.delete_artist_id
    WHERE EXISTS (
        SELECT 1 FROM track t_existing
        WHERE t_existing.artist_id = acrtu.keep_artist_id
          AND t_existing.name = t_dup.name
    )
    
    UNION
    
    -- Delete duplicate tracks among those being transferred (keep only one per name+target_artist)
    SELECT t_dup.id
    FROM track t_dup
    JOIN artist_case_relationships_to_update acrtu ON t_dup.artist_id = acrtu.delete_artist_id
    WHERE t_dup.id NOT IN (
        -- Keep the oldest track for each name+target_artist combination
        SELECT MIN(t_inner.id)
        FROM track t_inner
        JOIN artist_case_relationships_to_update acrtu_inner ON t_inner.artist_id = acrtu_inner.delete_artist_id
        WHERE acrtu_inner.keep_artist_id = acrtu.keep_artist_id
          AND t_inner.name = t_dup.name
        GROUP BY acrtu_inner.keep_artist_id, t_inner.name
    )
);

-- Then, update remaining tracks (those that won't create conflicts)
UPDATE track
SET artist_id = acrtu.keep_artist_id, updated_at = NOW()
FROM artist_case_relationships_to_update acrtu
WHERE track.artist_id = acrtu.delete_artist_id;

-- Update api_call entity references (where entity_type = 1 for ARTIST)
UPDATE api_call
SET entity_id = acrtu.keep_artist_id, updated_at = NOW()
FROM artist_case_relationships_to_update acrtu
WHERE api_call.entity_type = 1
  AND api_call.entity_id = acrtu.delete_artist_id;

-- Delete relationships of duplicate artists
DELETE FROM artist_tag WHERE artist_id IN (
    SELECT UNNEST(delete_artist_ids) FROM artist_case_duplicates_analysis
);

DELETE FROM artist_artist WHERE source_artist_id IN (
    SELECT UNNEST(delete_artist_ids) FROM artist_case_duplicates_analysis
) OR target_artist_id IN (
    SELECT UNNEST(delete_artist_ids) FROM artist_case_duplicates_analysis
);

DELETE FROM artist_album WHERE artist_id IN (
    SELECT UNNEST(delete_artist_ids) FROM artist_case_duplicates_analysis
);

DELETE FROM artist_track WHERE artist_id IN (
    SELECT UNNEST(delete_artist_ids) FROM artist_case_duplicates_analysis
);

-- Delete duplicate artists
DELETE FROM artist WHERE id IN (
    SELECT UNNEST(delete_artist_ids) FROM artist_case_duplicates_analysis
);

-- Log results
DO $$
DECLARE
    artists_removed INTEGER;
    duplicate_groups INTEGER;
    sample_duplicates TEXT;
BEGIN
    SELECT COUNT(*) INTO artists_removed FROM (
                                                  SELECT UNNEST(delete_artist_ids) FROM artist_case_duplicates_analysis
                                              ) a;

    SELECT COUNT(*) INTO duplicate_groups FROM artist_case_duplicates_analysis;

    -- Get sample of duplicates for logging
    SELECT STRING_AGG(
                   lower_name || ' (' || duplicate_count || ' variants: ' ||
                   ARRAY_TO_STRING(keep_artist_name || delete_artist_names, ' | ') || ')',
                   '; '
           ) INTO sample_duplicates
    FROM (
             SELECT * FROM artist_case_duplicates_analysis
             ORDER BY duplicate_count DESC
             LIMIT 5
         ) sample;

    RAISE NOTICE 'Eliminated % duplicate artists from % case-insensitive groups', artists_removed, duplicate_groups;
    RAISE NOTICE 'Sample duplicates: %', sample_duplicates;
END;
$$;
