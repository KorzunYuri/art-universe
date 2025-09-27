CREATE TABLE track (
    id BIGSERIAL PRIMARY KEY,
    reference_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE SEQUENCE track_seq START 1 INCREMENT BY 50;

-- Index for efficient JOIN with mu.track
CREATE INDEX idx_track_reference_id ON track(reference_id);

-- Comment on table and columns
COMMENT ON TABLE track IS 'Tracks approved for quiz participation';
COMMENT ON COLUMN track.reference_id IS 'Reference to id in mu.track table';
