-- Delete duplicate tracks that would conflict after URL normalization

DELETE FROM track 
WHERE id IN (SELECT duplicate_track_id FROM track_duplicates);
