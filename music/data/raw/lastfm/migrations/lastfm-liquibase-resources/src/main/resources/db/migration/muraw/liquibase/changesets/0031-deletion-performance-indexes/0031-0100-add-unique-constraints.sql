-- Add unique constraints to prevent future duplicates
-- These constraints will ensure data integrity after duplicate elimination

-- Add unique constraint for tracks (name + artist_id combination)
-- This prevents duplicate tracks with the same name for the same artist
ALTER TABLE track
    DROP CONSTRAINT IF EXISTS track_u_name_artist_id;

ALTER TABLE track
    ADD CONSTRAINT track_u_name_artist_id
        UNIQUE (name, artist_id);

-- Add unique constraint for albums (name + artist_id combination)  
-- This prevents duplicate albums with the same name for the same artist
ALTER TABLE album
    DROP CONSTRAINT IF EXISTS album_u_name_artist_id;

ALTER TABLE album
    ADD CONSTRAINT album_u_name_artist_id
        UNIQUE (name, artist_id);

-- Add unique constraint for artist_track relationships
-- This prevents duplicate artist-track relationships
ALTER TABLE artist_track
    DROP CONSTRAINT IF EXISTS artist_track_u_artist_id_track_id;

ALTER TABLE artist_track
    ADD CONSTRAINT artist_track_u_artist_id_track_id
        UNIQUE (artist_id, track_id);

-- Add unique constraint for artist_album relationships
-- This prevents duplicate artist-album relationships
ALTER TABLE artist_album
    DROP CONSTRAINT IF EXISTS artist_album_u_artist_id_album_id;

ALTER TABLE artist_album
    ADD CONSTRAINT artist_album_u_artist_id_album_id
        UNIQUE (artist_id, album_id);

-- Add unique constraint for album_track relationships
-- This prevents duplicate album-track relationships (same track in same album)
ALTER TABLE album_track
    DROP CONSTRAINT IF EXISTS album_track_u_album_id_track_id;

ALTER TABLE album_track
    ADD CONSTRAINT album_track_u_album_id_track_id
        UNIQUE (album_id, track_id);

-- Add unique constraint for track_tag relationships
-- This prevents duplicate track-tag relationships
ALTER TABLE track_tag
    DROP CONSTRAINT IF EXISTS track_tag_u_track_id_tag_id;

ALTER TABLE track_tag
    ADD CONSTRAINT track_tag_u_track_id_tag_id
        UNIQUE (track_id, tag_id);

-- Add unique constraint for album_tag relationships
-- This prevents duplicate album-tag relationships
ALTER TABLE album_tag
    DROP CONSTRAINT IF EXISTS album_tag_u_album_id_tag_id;

ALTER TABLE album_tag
    ADD CONSTRAINT album_tag_u_album_id_tag_id
        UNIQUE (album_id, tag_id);

-- Add unique constraint for artist_tag relationships
-- This prevents duplicate artist-tag relationships
ALTER TABLE artist_tag
    DROP CONSTRAINT IF EXISTS artist_tag_u_artist_id_tag_id;

ALTER TABLE artist_tag
    ADD CONSTRAINT artist_tag_u_artist_id_tag_id
        UNIQUE (artist_id, tag_id);

-- Log the constraints added
DO $$
BEGIN
    RAISE NOTICE 'Added unique constraints to prevent future duplicates';
    RAISE NOTICE 'Entity constraints: track_u_name_artist_id, album_u_name_artist_id';
    RAISE NOTICE 'Relationship constraints: artist_track_u_artist_id_track_id, artist_album_u_artist_id_album_id, album_track_u_album_id_track_id';
    RAISE NOTICE 'Tag relationship constraints: track_tag_u_track_id_tag_id, album_tag_u_album_id_tag_id, artist_tag_u_artist_id_tag_id';
END;
$$;
