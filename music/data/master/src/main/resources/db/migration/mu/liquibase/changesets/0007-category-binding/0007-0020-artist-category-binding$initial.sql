
-- Create artist_category_binding table
CREATE TABLE artist_category_binding (
    id BIGINT PRIMARY KEY,
    reference_id BIGINT NOT NULL,
    data_source_id INTEGER NOT NULL,
    external_artist_id BIGINT NOT NULL,
    external_category_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT artist_category_FK_reference
        FOREIGN KEY (reference_id)
            REFERENCES artist_category(id)
            ON DELETE CASCADE,
    CONSTRAINT artist_category_UK_binding
        UNIQUE (data_source_id, external_artist_id, external_category_id)
);

-- Create sequence for artist_category_binding
CREATE SEQUENCE artist_category_binding_seq INCREMENT BY 50 START WITH 1;

CREATE INDEX idx_artist_category_binding_reference_id
    ON artist_category_binding(reference_id);

CREATE INDEX idx_artist_category_binding_data_source
    ON artist_category_binding(data_source_id);

CREATE INDEX idx_artist_category_binding_external_artist_id
    ON artist_category_binding(external_artist_id);

CREATE INDEX idx_artist_category_binding_external_category_id
    ON artist_category_binding(external_category_id);
