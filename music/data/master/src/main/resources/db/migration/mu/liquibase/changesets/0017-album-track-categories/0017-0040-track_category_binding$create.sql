-- Create track_category_binding table
CREATE TABLE IF NOT EXISTS track_category_binding (
    id BIGINT PRIMARY KEY,
    master_id BIGINT NOT NULL,
    data_source_id INTEGER NOT NULL,
    external_track_id BIGINT NOT NULL,
    external_category_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_track_category_binding_master
        FOREIGN KEY (master_id)
            REFERENCES track_category(id)
            ON DELETE CASCADE,
    CONSTRAINT uk_track_category_binding
        UNIQUE (data_source_id, external_track_id, external_category_id)
);

-- Create sequence for track_category_binding
CREATE SEQUENCE IF NOT EXISTS track_category_binding_seq INCREMENT BY 50 START WITH 1;

CREATE INDEX IF NOT EXISTS idx_track_category_binding_master_id
    ON track_category_binding(master_id);

CREATE INDEX IF NOT EXISTS idx_track_category_binding_data_source
    ON track_category_binding(data_source_id);

CREATE INDEX IF NOT EXISTS idx_track_category_binding_external_track_id
    ON track_category_binding(external_track_id);

CREATE INDEX IF NOT EXISTS idx_track_category_binding_external_category_id
    ON track_category_binding(external_category_id);
