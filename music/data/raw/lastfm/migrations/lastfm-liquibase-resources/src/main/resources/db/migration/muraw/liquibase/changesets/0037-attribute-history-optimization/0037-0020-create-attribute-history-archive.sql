-- Create attribute_history_archive table for cold data (historical values)
-- This table stores expired attribute values with their validity period
-- Append-only: records are inserted but never updated
--
-- Index strategy: Minimal indexes for maximum write performance.
-- The processor only INSERTs into this table and never queries it.
-- The unique constraint is kept for data integrity (will fail on duplicate inserts,
-- highlighting potential flaws in processor logic).
-- Additional read indexes can be added later when read use cases are implemented.

CREATE TABLE mu_raw_lastfm.attribute_history_archive (
    id                  BIGINT                              NOT NULL,
    entity_type         SMALLINT                            NOT NULL,
    entity_id           BIGINT                              NOT NULL,
    attribute_id        SMALLINT                            NOT NULL,
    scope_entity_type   SMALLINT,
    scope_entity_id     BIGINT,
    string_value        VARCHAR(4096),
    numeric_value       BIGINT,
    collection_ts       TIMESTAMP                           NOT NULL,
    api_call_id         BIGINT,
    valid_from          DATE                                NOT NULL,
    valid_till          DATE                                NOT NULL,
    CONSTRAINT attribute_history_archive_pk
        PRIMARY KEY (id)
);

-- Unique constraint for data integrity
-- Ensures no duplicate historical records (same entity+attribute+validity period)
-- Will fail on duplicate inserts, highlighting potential flaws in processor logic
CREATE UNIQUE INDEX attribute_history_archive_uk_valid_till
    ON mu_raw_lastfm.attribute_history_archive
    (entity_type, entity_id, scope_entity_type, scope_entity_id, attribute_id, valid_till);

COMMENT ON TABLE mu_raw_lastfm.attribute_history_archive IS
    'Cold table storing historical (expired) attribute values. Append-only table for completed validity periods.';
