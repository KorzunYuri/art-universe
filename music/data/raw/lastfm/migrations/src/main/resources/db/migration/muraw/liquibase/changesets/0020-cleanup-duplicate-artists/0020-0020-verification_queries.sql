-- Verification queries to run after the duplicate artists cleanup migration
-- These queries help verify that the migration was successful

-- 1. Check remaining duplicates (should be significantly reduced or zero)
SELECT 
    'Remaining duplicate artists' as check_name,
    mbid, 
    COUNT(*) as remaining_count,
    STRING_AGG(DISTINCT name, '; ') as artist_names
FROM artist 
WHERE mbid IS NOT NULL AND mbid != ''
GROUP BY mbid 
HAVING COUNT(*) > 1
ORDER BY remaining_count DESC
LIMIT 10;

-- 2. Check artists without any relations (orphaned artists)
SELECT 
    'Orphaned artists by API call type' as check_name,
    ac.type as api_call_type, 
    COUNT(*) as orphaned_artists
FROM artist a
JOIN api_call ac ON a.api_call_id = ac.id
WHERE NOT EXISTS (SELECT 1 FROM track t WHERE t.artist_id = a.id)
  AND NOT EXISTS (SELECT 1 FROM artist_track at WHERE at.artist_id = a.id)
GROUP BY ac.type
ORDER BY orphaned_artists DESC;

-- 3. Check track distribution by artist source
SELECT 
    'Track distribution by artist source' as check_name,
    ac.type as artist_source, 
    COUNT(DISTINCT t.id) as track_count
FROM track t
JOIN artist a ON t.artist_id = a.id
JOIN api_call ac ON a.api_call_id = ac.id
GROUP BY ac.type
ORDER BY track_count DESC;

-- 4. Check artist_track relations by artist source
SELECT 
    'Artist-track relations by artist source' as check_name,
    ac.type as artist_source, 
    COUNT(*) as relation_count
FROM artist_track at
JOIN artist a ON at.artist_id = a.id
JOIN api_call ac ON a.api_call_id = ac.id
GROUP BY ac.type
ORDER BY relation_count DESC;

-- 5. Sample check for specific MBID (Lil Wayne example)
SELECT 
    'Lil Wayne artists after cleanup' as check_name,
    a.id, 
    a.name, 
    a.mbid, 
    ac.type as api_call_type,
    (SELECT COUNT(*) FROM track t WHERE t.artist_id = a.id) as tracks_via_artist_id,
    (SELECT COUNT(*) FROM artist_track at WHERE at.artist_id = a.id) as tracks_via_artist_track
FROM artist a
JOIN api_call ac ON a.api_call_id = ac.id
WHERE a.mbid = 'ac9a487a-d9d2-4f27-bb23-0f4686488345'
ORDER BY a.id;

-- 6. Overall statistics
SELECT 
    'Overall statistics' as check_name,
    'Total artists' as metric,
    COUNT(*) as value
FROM artist
UNION ALL
SELECT 
    'Overall statistics' as check_name,
    'Artists with MBID' as metric,
    COUNT(*) as value
FROM artist 
WHERE mbid IS NOT NULL AND mbid != ''
UNION ALL
SELECT 
    'Overall statistics' as check_name,
    'Total tracks' as metric,
    COUNT(*) as value
FROM track
UNION ALL
SELECT 
    'Overall statistics' as check_name,
    'Total artist-track relations' as metric,
    COUNT(*) as value
FROM artist_track;
