-- Restore artist_id for tracks from artist_track relationship table
-- This fixes the issue where artist_id was set to NULL after migrations 0020 and 0021

-- Step 1: Restore artist_id from artist_track table for tracks that have relationships
UPDATE track 
SET artist_id = at.artist_id
FROM artist_track at
WHERE track.id = at.track_id
  AND track.artist_id IS NULL;

-- Step 2: Log statistics for verification
DO $$
DECLARE
    total_tracks INTEGER;
    restored_tracks INTEGER;
    remaining_null INTEGER;
    recovery_percentage DECIMAL(5,2);
BEGIN
    SELECT COUNT(*) INTO total_tracks FROM track;
    SELECT COUNT(*) INTO restored_tracks FROM track WHERE artist_id IS NOT NULL;
    SELECT COUNT(*) INTO remaining_null FROM track WHERE artist_id IS NULL;
    
    RAISE NOTICE 'Track artist_id restoration completed:';
    RAISE NOTICE '  Total tracks: %', total_tracks;
    RAISE NOTICE '  Tracks with artist_id: %', restored_tracks;
    RAISE NOTICE '  Tracks without artist_id: %', remaining_null;

    IF total_tracks > 0 THEN
        recovery_percentage := (restored_tracks::DECIMAL / total_tracks * 100);
        RAISE NOTICE '  Recovery percentage: %.2f%%', recovery_percentage;
    ELSE
        RAISE NOTICE '  Recovery percentage: N/A (no tracks)';
    END IF;
END $$;
