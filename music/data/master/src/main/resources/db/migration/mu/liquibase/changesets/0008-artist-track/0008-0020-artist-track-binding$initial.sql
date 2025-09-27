-- Create artist_track_binding table
CREATE TABLE artist_track_binding (
    id BIGINT PRIMARY KEY,
    reference_id BIGINT NOT NULL,
    data_source_id INTEGER NOT NULL,
    external_artist_id BIGINT NOT NULL,
    external_track_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT artist_track_FK_reference
        FOREIGN KEY (reference_id)
            REFERENCES artist_track(id)
            ON DELETE CASCADE,
    CONSTRAINT artist_track_UK_binding
        UNIQUE (data_source_id, external_artist_id, external_track_id)
);

-- Create sequence for artist_track_binding
CREATE SEQUENCE artist_track_binding_seq INCREMENT BY 50 START WITH 1;

-- Create indexes for better performance
CREATE INDEX idx_artist_track_binding_reference_id
    ON artist_track_binding(reference_id);

CREATE INDEX idx_artist_track_binding_data_source
    ON artist_track_binding(data_source_id);

CREATE INDEX idx_artist_track_binding_external_artist_id
    ON artist_track_binding(external_artist_id);

CREATE INDEX idx_artist_track_binding_external_track_id
    ON artist_track_binding(external_track_id);
