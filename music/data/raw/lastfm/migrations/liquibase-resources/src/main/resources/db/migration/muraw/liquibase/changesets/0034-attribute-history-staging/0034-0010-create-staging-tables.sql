-- Create staging tables for attribute history double buffering
DROP TABLE IF EXISTS mu_raw_lastfm_staging.stg_attribute_history_a;
CREATE TABLE mu_raw_lastfm_staging.stg_attribute_history_a (
    id BIGSERIAL PRIMARY KEY,
    api_call_id BIGINT NOT NULL,
    entity_type INTEGER NOT NULL,
    entity_id BIGINT NOT NULL,
    attribute_id INTEGER NOT NULL,
    scope_entity_type INTEGER,
    scope_entity_id BIGINT,
    string_value TEXT,
    numeric_value BIGINT,
    collection_ts TIMESTAMP NOT NULL DEFAULT NOW(),
    valid_from DATE NOT NULL DEFAULT CURRENT_DATE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

DROP TABLE IF EXISTS mu_raw_lastfm_staging.stg_attribute_history_b;
CREATE TABLE mu_raw_lastfm_staging.stg_attribute_history_b (
    id BIGSERIAL PRIMARY KEY,
    api_call_id BIGINT NOT NULL,
    entity_type INTEGER NOT NULL,
    entity_id BIGINT NOT NULL,
    attribute_id INTEGER NOT NULL,
    scope_entity_type INTEGER,
    scope_entity_id BIGINT,
    string_value TEXT,
    numeric_value BIGINT,
    collection_ts TIMESTAMP NOT NULL DEFAULT NOW(),
    valid_from DATE NOT NULL DEFAULT CURRENT_DATE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);
