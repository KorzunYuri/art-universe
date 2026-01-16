-- Create a sequence for new IDs (shared between current and archive)
DROP SEQUENCE IF EXISTS mu_raw_lastfm.attribute_history_seq;

CREATE SEQUENCE mu_raw_lastfm.attribute_history_seq
    START WITH 1
    INCREMENT BY 1;

-- Set the sequence to continue from the max ID in the legacy table
SELECT setval('mu_raw_lastfm.attribute_history_seq',
              COALESCE((SELECT MAX(id) FROM mu_raw_lastfm.attribute_history_legacy), 0) + 1);
