-- Populate artist_id in album table from artist_album relationships
UPDATE album a
SET artist_id = (
    SELECT aa.artist_id 
    FROM artist_album aa 
    WHERE aa.album_id = a.id 
    ORDER BY aa.created_at DESC 
    LIMIT 1
)
WHERE EXISTS (
    SELECT 1 
    FROM artist_album aa 
    WHERE aa.album_id = a.id
);
