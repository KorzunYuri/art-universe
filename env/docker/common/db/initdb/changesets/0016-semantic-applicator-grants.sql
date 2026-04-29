-- Grants for mu_sa_dm to write into the mu schema (semantic-applicator).
--
-- 0015 gave mu_sa_dm SELECT on mu (needed by analyzer/parser for category tree
-- and entity matching). The applicator must also INSERT/UPDATE into mu tables
-- (artist, album, track, category, *_category, *_binding, relation tables,
-- entity_attribute_value, attribute_def, dictionary, ...) and allocate IDs
-- from mu_seq sequences, hence USAGE + SELECT on sequences.

GRANT INSERT, UPDATE, DELETE ON ALL TABLES    IN SCHEMA mu TO mu_sa_dm;
GRANT USAGE, SELECT            ON ALL SEQUENCES IN SCHEMA mu TO mu_sa_dm;

ALTER DEFAULT PRIVILEGES IN SCHEMA mu GRANT INSERT, UPDATE, DELETE ON TABLES    TO mu_sa_dm;
ALTER DEFAULT PRIVILEGES IN SCHEMA mu GRANT USAGE, SELECT            ON SEQUENCES TO mu_sa_dm;
