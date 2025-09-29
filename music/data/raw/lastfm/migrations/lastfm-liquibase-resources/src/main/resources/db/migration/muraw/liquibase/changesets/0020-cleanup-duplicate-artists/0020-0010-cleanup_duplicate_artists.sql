-- Migration to clean up duplicate artist records and all related relations
-- 
-- Problem: Multiple artist records exist with the same MBID, created through different API methods.
-- Artists created via TAG_TOP_TRACKS (type 3) are considered canonical.
-- 
-- Strategy:
-- 1. Identify canonical artists (from TAG_TOP_TRACKS where possible, otherwise oldest by ID)
-- 2. Update all foreign key references to point to canonical artists
-- 3. Remove duplicate relations in all artist_* tables
-- 4. Remove orphaned artist records that have no tracks and no relations

-- Step 1: Create a temporary table with canonical artist mappings
CREATE TEMP TABLE canonical_artist_mapping AS
WITH duplicate_groups AS (
    -- Find all groups of artists with the same MBID
    SELECT mbid, ARRAY_AGG(id ORDER BY id) as artist_ids
    FROM artist 
    WHERE mbid IS NOT NULL AND mbid != ''
    GROUP BY mbid 
    HAVING COUNT(*) > 1
),
canonical_selection AS (
    -- For each group, select the canonical artist
    SELECT 
        dg.mbid,
        dg.artist_ids,
        COALESCE(
            -- Prefer artist created via TAG_TOP_TRACKS (type 3)
            (SELECT a.id 
             FROM artist a 
             JOIN api_call ac ON a.api_call_id = ac.id 
             WHERE a.id = ANY(dg.artist_ids) AND ac.type = 3 
             ORDER BY a.id 
             LIMIT 1),
            -- Otherwise, take the oldest (smallest ID)
            dg.artist_ids[1]
        ) as canonical_artist_id
    FROM duplicate_groups dg
)
SELECT 
    cs.mbid,
    cs.canonical_artist_id,
    UNNEST(cs.artist_ids) as duplicate_artist_id
FROM canonical_selection cs;

-- Add index for performance
CREATE INDEX idx_canonical_mapping ON canonical_artist_mapping(duplicate_artist_id);

-- Step 2: Update track.artist_id to point to canonical artists
UPDATE track 
SET artist_id = cam.canonical_artist_id
FROM canonical_artist_mapping cam
WHERE track.artist_id = cam.duplicate_artist_id 
  AND cam.duplicate_artist_id != cam.canonical_artist_id;

-- Step 3: Update artist_track relations
-- Remove duplicate relations first, then update remaining ones
DELETE FROM artist_track at
USING canonical_artist_mapping cam
WHERE at.artist_id = cam.duplicate_artist_id 
  AND cam.duplicate_artist_id != cam.canonical_artist_id;

-- Step 4: Update artist_tag relations
-- Remove duplicate relations first, then update remaining ones
DELETE FROM artist_tag at
USING canonical_artist_mapping cam
WHERE at.artist_id = cam.duplicate_artist_id 
  AND cam.duplicate_artist_id != cam.canonical_artist_id;

-- Step 5: Update artist_album relations
-- Remove duplicate relations first, then update remaining ones
DELETE FROM artist_album aa
USING canonical_artist_mapping cam
WHERE aa.artist_id = cam.duplicate_artist_id 
  AND cam.duplicate_artist_id != cam.canonical_artist_id;

-- Step 6: Handle artist_artist relations (both source and target)
-- First, remove relations that would become duplicates after update
DELETE FROM artist_artist aa1
USING canonical_artist_mapping cam1, canonical_artist_mapping cam2
WHERE (aa1.source_artist_id = cam1.duplicate_artist_id AND cam1.duplicate_artist_id != cam1.canonical_artist_id)
   OR (aa1.target_artist_id = cam2.duplicate_artist_id AND cam2.duplicate_artist_id != cam2.canonical_artist_id)
   AND EXISTS (
       SELECT 1 FROM artist_artist aa2
       WHERE aa2.id != aa1.id
         AND aa2.source_artist_id = COALESCE(
             CASE WHEN aa1.source_artist_id = cam1.duplicate_artist_id THEN cam1.canonical_artist_id ELSE aa1.source_artist_id END,
             aa1.source_artist_id
         )
         AND aa2.target_artist_id = COALESCE(
             CASE WHEN aa1.target_artist_id = cam2.duplicate_artist_id THEN cam2.canonical_artist_id ELSE aa1.target_artist_id END,
             aa1.target_artist_id
         )
         AND aa2.relation_type = aa1.relation_type
   );

-- Update source_artist_id references
UPDATE artist_artist 
SET source_artist_id = cam.canonical_artist_id
FROM canonical_artist_mapping cam
WHERE artist_artist.source_artist_id = cam.duplicate_artist_id 
  AND cam.duplicate_artist_id != cam.canonical_artist_id;

-- Update target_artist_id references
UPDATE artist_artist 
SET target_artist_id = cam.canonical_artist_id
FROM canonical_artist_mapping cam
WHERE artist_artist.target_artist_id = cam.duplicate_artist_id 
  AND cam.duplicate_artist_id != cam.canonical_artist_id;

-- Remove self-referencing relations and any remaining duplicates
DELETE FROM artist_artist aa1
WHERE aa1.source_artist_id = aa1.target_artist_id
   OR EXISTS (
       SELECT 1 FROM artist_artist aa2 
       WHERE aa2.id < aa1.id 
         AND aa2.source_artist_id = aa1.source_artist_id 
         AND aa2.target_artist_id = aa1.target_artist_id
         AND aa2.relation_type = aa1.relation_type
   );

-- Step 7: Clean up orphaned artists
-- Remove artists that:
-- - Are duplicates (not canonical)
-- - Have no tracks via artist_id
-- - Have no relations in any artist_* table
DELETE FROM artist a
USING canonical_artist_mapping cam
WHERE a.id = cam.duplicate_artist_id 
  AND cam.duplicate_artist_id != cam.canonical_artist_id
  AND NOT EXISTS (SELECT 1 FROM track t WHERE t.artist_id = a.id)
  AND NOT EXISTS (SELECT 1 FROM artist_track at WHERE at.artist_id = a.id)
  AND NOT EXISTS (SELECT 1 FROM artist_tag at WHERE at.artist_id = a.id)
  AND NOT EXISTS (SELECT 1 FROM artist_album aa WHERE aa.artist_id = a.id)
  AND NOT EXISTS (SELECT 1 FROM artist_artist aa WHERE aa.source_artist_id = a.id OR aa.target_artist_id = a.id);

-- Step 8: Update statistics
ANALYZE artist;
ANALYZE track;
ANALYZE artist_track;
ANALYZE artist_tag;
ANALYZE artist_album;
ANALYZE artist_artist;
