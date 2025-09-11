-- Create category_category table for many-to-many relationships
CREATE TABLE category_category (
    id BIGINT PRIMARY KEY,
    source_category_id BIGINT NOT NULL,
    target_category_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_category_category_source FOREIGN KEY (source_category_id) REFERENCES category(id) ON DELETE CASCADE,
    CONSTRAINT fk_category_category_target FOREIGN KEY (target_category_id) REFERENCES category(id) ON DELETE CASCADE,
    CONSTRAINT uk_category_category UNIQUE (source_category_id, target_category_id)
);

-- Create sequence for category_category
CREATE SEQUENCE category_category_seq INCREMENT BY 10 START WITH 1;

-- Create indexes for better performance
CREATE INDEX idx_category_category_source_id
    ON category_category(source_category_id);

CREATE INDEX idx_category_category_target_id
    ON category_category(target_category_id);
