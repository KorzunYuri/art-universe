-- Default privileges for mu_raw_lastfm_dm on both lastfm schemas.
-- Also grants read access to mu_raw_lastfm_reader (current + future tables).
-- Runs as mu_raw_lastfm_dm (the schema owner).

ALTER DEFAULT PRIVILEGES IN SCHEMA mu_raw_lastfm         GRANT ALL ON TABLES    TO mu_raw_lastfm_dm;
ALTER DEFAULT PRIVILEGES IN SCHEMA mu_raw_lastfm         GRANT ALL ON SEQUENCES TO mu_raw_lastfm_dm;
ALTER DEFAULT PRIVILEGES IN SCHEMA mu_raw_lastfm         GRANT ALL ON FUNCTIONS TO mu_raw_lastfm_dm;
ALTER DEFAULT PRIVILEGES IN SCHEMA mu_raw_lastfm_staging GRANT ALL ON TABLES    TO mu_raw_lastfm_dm;
ALTER DEFAULT PRIVILEGES IN SCHEMA mu_raw_lastfm_staging GRANT ALL ON SEQUENCES TO mu_raw_lastfm_dm;
ALTER DEFAULT PRIVILEGES IN SCHEMA mu_raw_lastfm_staging GRANT ALL ON FUNCTIONS TO mu_raw_lastfm_dm;

-- Reader: schema usage + existing tables + future tables
GRANT USAGE ON SCHEMA mu_raw_lastfm         TO mu_raw_lastfm_reader;
GRANT USAGE ON SCHEMA mu_raw_lastfm_staging TO mu_raw_lastfm_reader;
GRANT SELECT ON ALL TABLES IN SCHEMA mu_raw_lastfm         TO mu_raw_lastfm_reader;
GRANT SELECT ON ALL TABLES IN SCHEMA mu_raw_lastfm_staging TO mu_raw_lastfm_reader;
ALTER DEFAULT PRIVILEGES IN SCHEMA mu_raw_lastfm         GRANT SELECT ON TABLES TO mu_raw_lastfm_reader;
ALTER DEFAULT PRIVILEGES IN SCHEMA mu_raw_lastfm_staging GRANT SELECT ON TABLES TO mu_raw_lastfm_reader;
