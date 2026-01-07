-- Drop existing RESTRICT constraints
ALTER TABLE album DROP CONSTRAINT album_fk_artist;
ALTER TABLE track DROP CONSTRAINT track_fk_artist;

-- Add new CASCADE constraints
ALTER TABLE album 
ADD CONSTRAINT album_fk_artist 
FOREIGN KEY (primary_artist_id) REFERENCES artist(id) ON DELETE CASCADE;

ALTER TABLE track 
ADD CONSTRAINT track_fk_artist 
FOREIGN KEY (primary_artist_id) REFERENCES artist(id) ON DELETE CASCADE;
