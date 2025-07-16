-- Create artist_track table
CREATE TABLE artist_track (
    id BIGINT PRIMARY KEY,
    artist_id BIGINT NOT NULL,
    track_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_artist_track_artist FOREIGN KEY (artist_id) REFERENCES artist(id) ON DELETE CASCADE,
    CONSTRAINT fk_artist_track_track FOREIGN KEY (track_id) REFERENCES track(id) ON DELETE CASCADE,
    CONSTRAINT uk_artist_track UNIQUE (artist_id, track_id)
);

-- Create sequence for artist_track
CREATE SEQUENCE artist_track_seq INCREMENT BY 50 START WITH 1;

-- Create indexes for better performance
CREATE INDEX idx_artist_track_artist_id
    ON artist_track(artist_id);

CREATE INDEX idx_artist_track_track_id
    ON artist_track(track_id);
