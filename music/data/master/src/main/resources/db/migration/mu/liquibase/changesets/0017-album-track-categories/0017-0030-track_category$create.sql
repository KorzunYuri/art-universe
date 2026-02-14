-- Create track_category table
CREATE TABLE track_category (
    id BIGINT PRIMARY KEY,
    track_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_track_category_track FOREIGN KEY (track_id) REFERENCES track(id) ON DELETE CASCADE,
    CONSTRAINT fk_track_category_category FOREIGN KEY (category_id) REFERENCES category(id) ON DELETE CASCADE,
    CONSTRAINT uk_track_category UNIQUE (track_id, category_id)
);

-- Create sequence for track_category
CREATE SEQUENCE track_category_seq INCREMENT BY 50 START WITH 1;

-- Create indexes for better performance
CREATE INDEX idx_track_category_track_id
    ON track_category(track_id);

CREATE INDEX idx_track_category_category_id
    ON track_category(category_id);
