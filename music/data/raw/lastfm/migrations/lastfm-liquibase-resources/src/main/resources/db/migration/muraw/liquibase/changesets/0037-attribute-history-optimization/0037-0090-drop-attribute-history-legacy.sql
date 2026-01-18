-- Verify data migration before dropping the legacy table
-- This verification ensures no data was lost during migration

DO $$
DECLARE
    legacy_count BIGINT;
    current_count BIGINT;
    archive_count BIGINT;
    new_total BIGINT;
BEGIN
    SELECT COUNT(*) INTO legacy_count FROM mu_raw_lastfm.attribute_history_legacy;
    SELECT COUNT(*) INTO current_count FROM mu_raw_lastfm.attribute_history_current;
    SELECT COUNT(*) INTO archive_count FROM mu_raw_lastfm.attribute_history_archive;
    new_total := current_count + archive_count;

    RAISE NOTICE 'Legacy table count: %', legacy_count;
    RAISE NOTICE 'Current table count: %', current_count;
    RAISE NOTICE 'Archive table count: %', archive_count;
    RAISE NOTICE 'New total count: %', new_total;

    IF legacy_count != new_total THEN
        RAISE EXCEPTION 'Data migration verification failed! Legacy: %, New total: %', legacy_count, new_total;
    END IF;

    RAISE NOTICE 'Data migration verified successfully.';
END $$;

-- Drop the legacy table and its sequence
DROP TABLE mu_raw_lastfm.attribute_history_legacy CASCADE;
DROP SEQUENCE IF EXISTS mu_raw_lastfm.attribute_history_legacy_seq;

-- Analyze new tables for query optimizer
ANALYZE mu_raw_lastfm.attribute_history_current;
ANALYZE mu_raw_lastfm.attribute_history_archive;
