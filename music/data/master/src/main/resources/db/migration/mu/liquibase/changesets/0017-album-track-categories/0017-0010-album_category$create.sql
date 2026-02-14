-- Create album_category table
CREATE TABLE album_category (
    id BIGINT PRIMARY KEY,
    album_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_album_category_album FOREIGN KEY (album_id) REFERENCES album(id) ON DELETE CASCADE,
    CONSTRAINT fk_album_category_category FOREIGN KEY (category_id) REFERENCES category(id) ON DELETE CASCADE,
    CONSTRAINT uk_album_category UNIQUE (album_id, category_id)
);

-- Create sequence for album_category
CREATE SEQUENCE album_category_seq INCREMENT BY 50 START WITH 1;

-- Create indexes for better performance
CREATE INDEX idx_album_category_album_id
    ON album_category(album_id);

CREATE INDEX idx_album_category_category_id
    ON album_category(category_id);
