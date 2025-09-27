-- Delete relationships of duplicate tracks that would conflict after URL normalization

-- Delete artist_track relationships
DELETE FROM artist_track 
WHERE track_id IN (SELECT duplicate_track_id FROM track_duplicates);

-- Delete album_track relationships
DELETE FROM album_track 
WHERE track_id IN (SELECT duplicate_track_id FROM track_duplicates);

-- Delete track_tag relationships
DELETE FROM track_tag 
WHERE track_id IN (SELECT duplicate_track_id FROM track_duplicates);
