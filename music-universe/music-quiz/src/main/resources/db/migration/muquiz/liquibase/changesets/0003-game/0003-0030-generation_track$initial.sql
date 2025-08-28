DROP SEQUENCE IF EXISTS generation_track_seq;

DROP INDEX IF EXISTS idx_generation_track_generation_id;
DROP INDEX IF EXISTS idx_generation_track_track_id;

DROP TABLE IF EXISTS generation_track;

CREATE TABLE generation_track (
    id BIGSERIAL PRIMARY KEY,
    generation_id BIGINT NOT NULL REFERENCES generation(id),
    track_id BIGINT NOT NULL,
    primary_artist_id BIGINT NOT NULL,
    track_name VARCHAR(500) NOT NULL,
    artist_name VARCHAR(500) NOT NULL,
    order_index INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE SEQUENCE generation_track_seq START 1 INCREMENT BY 50;

CREATE INDEX idx_generation_track_generation_id ON generation_track(generation_id);
CREATE INDEX idx_generation_track_track_id ON generation_track(track_id);

COMMENT ON TABLE generation_track IS 'Tracks in a generation with display details';
COMMENT ON COLUMN generation_track.order_index IS 'Display order in the generation';
