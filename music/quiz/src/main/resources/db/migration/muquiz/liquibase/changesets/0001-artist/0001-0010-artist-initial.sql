CREATE TABLE artist (
    id BIGSERIAL PRIMARY KEY,
    reference_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE SEQUENCE artist_seq START 1 INCREMENT BY 50;

-- Index for efficient JOIN with mu.artist
CREATE INDEX idx_artist_reference_id ON artist(reference_id);

-- Comment on table and columns
COMMENT ON TABLE artist IS 'Artists approved for quiz participation';
COMMENT ON COLUMN artist.reference_id IS 'Reference to id in mu.artist table';
