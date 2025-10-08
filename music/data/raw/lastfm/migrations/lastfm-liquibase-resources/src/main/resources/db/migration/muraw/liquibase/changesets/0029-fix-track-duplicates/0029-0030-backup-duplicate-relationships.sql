-- Create backup of duplicate track relationships before deletion

-- Create table for relationship backup
CREATE TEMP TABLE track_relationships_backup AS
SELECT 
    'artist_track' as relation_type,
    td.duplicate_track_id,
    td.original_track_id,
    at.id as relation_id,
    at.artist_id,
    NULL::bigint as album_id,
    NULL::bigint as tag_id,
    at.api_call_id
FROM track_duplicates td
JOIN artist_track at ON at.track_id = td.duplicate_track_id

UNION ALL

SELECT 
    'album_track' as relation_type,
    td.duplicate_track_id,
    td.original_track_id,
    alt.id,
    NULL::bigint,
    alt.album_id,
    NULL::bigint,
    alt.api_call_id
FROM track_duplicates td
JOIN album_track alt ON alt.track_id = td.duplicate_track_id

UNION ALL

SELECT 
    'track_tag' as relation_type,
    td.duplicate_track_id,
    td.original_track_id,
    tt.id,
    NULL::bigint,
    NULL::bigint,
    tt.tag_id,
    tt.api_call_id
FROM track_duplicates td
JOIN track_tag tt ON tt.track_id = td.duplicate_track_id;
