-- Add artist_id column to album table
ALTER TABLE album ADD COLUMN artist_id bigint;

-- Create foreign key constraint
ALTER TABLE album ADD CONSTRAINT fk_album_artist FOREIGN KEY (artist_id) REFERENCES artist(id);

-- Create index for better performance
CREATE INDEX idx_album_artist_id ON album(artist_id);
