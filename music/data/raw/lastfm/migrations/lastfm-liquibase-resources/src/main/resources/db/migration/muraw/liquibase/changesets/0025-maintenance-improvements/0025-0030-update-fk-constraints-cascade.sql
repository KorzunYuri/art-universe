-- Update foreign key constraints to CASCADE for automatic cleanup of relationship records

-- 1. artist_tag table
ALTER TABLE artist_tag DROP CONSTRAINT artist_tag_fk_artist;
ALTER TABLE artist_tag ADD CONSTRAINT artist_tag_fk_artist 
    FOREIGN KEY (artist_id) REFERENCES artist(id) ON DELETE CASCADE;

ALTER TABLE artist_tag DROP CONSTRAINT artist_tag_fk_tag;
ALTER TABLE artist_tag ADD CONSTRAINT artist_tag_fk_tag 
    FOREIGN KEY (tag_id) REFERENCES tag(id) ON DELETE CASCADE;

-- 2. artist_album table
ALTER TABLE artist_album DROP CONSTRAINT artist_album_fk_artist;
ALTER TABLE artist_album ADD CONSTRAINT artist_album_fk_artist 
    FOREIGN KEY (artist_id) REFERENCES artist(id) ON DELETE CASCADE;

ALTER TABLE artist_album DROP CONSTRAINT artist_album_fk_album;
ALTER TABLE artist_album ADD CONSTRAINT artist_album_fk_album 
    FOREIGN KEY (album_id) REFERENCES album(id) ON DELETE CASCADE;

-- 3. artist_artist table
ALTER TABLE artist_artist DROP CONSTRAINT artist_artist_fk_source_artist;
ALTER TABLE artist_artist ADD CONSTRAINT artist_artist_fk_source_artist 
    FOREIGN KEY (source_artist_id) REFERENCES artist(id) ON DELETE CASCADE;

ALTER TABLE artist_artist DROP CONSTRAINT artist_artist_fk_target_artist;
ALTER TABLE artist_artist ADD CONSTRAINT artist_artist_fk_target_artist 
    FOREIGN KEY (target_artist_id) REFERENCES artist(id) ON DELETE CASCADE;

-- 4. artist_track table
ALTER TABLE artist_track DROP CONSTRAINT artist_track_fk_artist;
ALTER TABLE artist_track ADD CONSTRAINT artist_track_fk_artist 
    FOREIGN KEY (artist_id) REFERENCES artist(id) ON DELETE CASCADE;

ALTER TABLE artist_track DROP CONSTRAINT artist_track_fk_track;
ALTER TABLE artist_track ADD CONSTRAINT artist_track_fk_track 
    FOREIGN KEY (track_id) REFERENCES track(id) ON DELETE CASCADE;

-- 5. album_tag table
ALTER TABLE album_tag DROP CONSTRAINT album_tag_fk_album;
ALTER TABLE album_tag ADD CONSTRAINT album_tag_fk_album 
    FOREIGN KEY (album_id) REFERENCES album(id) ON DELETE CASCADE;

ALTER TABLE album_tag DROP CONSTRAINT album_tag_fk_tag;
ALTER TABLE album_tag ADD CONSTRAINT album_tag_fk_tag 
    FOREIGN KEY (tag_id) REFERENCES tag(id) ON DELETE CASCADE;

-- 6. album_track table
ALTER TABLE album_track DROP CONSTRAINT album_track_fk_album;
ALTER TABLE album_track ADD CONSTRAINT album_track_fk_album 
    FOREIGN KEY (album_id) REFERENCES album(id) ON DELETE CASCADE;

ALTER TABLE album_track DROP CONSTRAINT album_track_fk_track;
ALTER TABLE album_track ADD CONSTRAINT album_track_fk_track 
    FOREIGN KEY (track_id) REFERENCES track(id) ON DELETE CASCADE;

-- 7. track_tag table
ALTER TABLE track_tag DROP CONSTRAINT track_tag_fk_track;
ALTER TABLE track_tag ADD CONSTRAINT track_tag_fk_track 
    FOREIGN KEY (track_id) REFERENCES track(id) ON DELETE CASCADE;

ALTER TABLE track_tag DROP CONSTRAINT track_tag_fk_tag;
ALTER TABLE track_tag ADD CONSTRAINT track_tag_fk_tag 
    FOREIGN KEY (tag_id) REFERENCES tag(id) ON DELETE CASCADE;
