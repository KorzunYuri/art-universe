-- Count of entities by type
SELECT 
    'artist' as entity_type,
    COUNT(*) as count
FROM mu_raw_lastfm.artist
UNION ALL
SELECT 
    'album' as entity_type,
    COUNT(*) as count
FROM mu_raw_lastfm.album
UNION ALL
SELECT 
    'track' as entity_type,
    COUNT(*) as count
FROM mu_raw_lastfm.track
UNION ALL
SELECT 
    'tag' as entity_type,
    COUNT(*) as count
FROM mu_raw_lastfm.tag;
