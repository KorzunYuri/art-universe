-- Create attribute_history_current table for hot data (current values only)
-- This table stores only current attribute values (implicitly valid_till = '9999-12-31')
-- No valid_till column needed - all records are current by definition
--
-- Index strategy: Minimal indexes for maximum write performance.
-- Only the unique constraint is needed for the processor's JOIN operation.
-- Additional read indexes can be added later when read use cases are implemented.

CREATE TABLE mu_raw_lastfm.attribute_history_current (
    id                  BIGINT                              NOT NULL,
    entity_type         SMALLINT                            NOT NULL,
    entity_id           BIGINT                              NOT NULL,
    attribute_id        SMALLINT                            NOT NULL,
    scope_entity_type   SMALLINT,
    scope_entity_id     BIGINT,
    string_value        VARCHAR(4096),
    numeric_value       BIGINT,
    collection_ts       TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    api_call_id         BIGINT,
    valid_from          DATE DEFAULT now()                  NOT NULL,
    CONSTRAINT attribute_history_current_pk
        PRIMARY KEY (id)
);

-- Unique constraint for deduplication and JOIN performance in LastfmAttributeHistoryProcessor
-- This index is critical for the processor's existing_values CTE join
CREATE UNIQUE INDEX attribute_history_current_uk
    ON mu_raw_lastfm.attribute_history_current
    (entity_type, entity_id, COALESCE(scope_entity_type, -1), COALESCE(scope_entity_id, -1), attribute_id);

COMMENT ON TABLE mu_raw_lastfm.attribute_history_current IS
    'Hot table storing current attribute values. All records are implicitly valid until further notice (valid_till = 9999-12-31).';
