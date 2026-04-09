-- Create artist_category table
CREATE TABLE artist_category (
    id BIGINT PRIMARY KEY,
    artist_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_artist_category_artist FOREIGN KEY (artist_id) REFERENCES artist(id) ON DELETE CASCADE,
    CONSTRAINT fk_artist_category_category FOREIGN KEY (category_id) REFERENCES category(id) ON DELETE CASCADE,
    CONSTRAINT uk_artist_category UNIQUE (artist_id, category_id)
);

-- Create sequence for artist_category
CREATE SEQUENCE artist_category_seq INCREMENT BY 50 START WITH 1;

-- Create indexes for better performance
CREATE INDEX idx_artist_category_artist_id
    ON artist_category(artist_id);

CREATE INDEX idx_artist_category_category_id
    ON artist_category(category_id);
