-- Create album_category_binding table
CREATE TABLE album_category_binding (
    id BIGINT PRIMARY KEY,
    master_id BIGINT NOT NULL,
    data_source_id INTEGER NOT NULL,
    external_album_id BIGINT NOT NULL,
    external_category_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_album_category_binding_master
        FOREIGN KEY (master_id)
            REFERENCES album_category(id)
            ON DELETE CASCADE,
    CONSTRAINT uk_album_category_binding
        UNIQUE (data_source_id, external_album_id, external_category_id)
);

-- Create sequence for album_category_binding
CREATE SEQUENCE album_category_binding_seq INCREMENT BY 50 START WITH 1;

CREATE INDEX idx_album_category_binding_master_id
    ON album_category_binding(master_id);

CREATE INDEX idx_album_category_binding_data_source
    ON album_category_binding(data_source_id);

CREATE INDEX idx_album_category_binding_external_album_id
    ON album_category_binding(external_album_id);

CREATE INDEX idx_album_category_binding_external_category_id
    ON album_category_binding(external_category_id);
