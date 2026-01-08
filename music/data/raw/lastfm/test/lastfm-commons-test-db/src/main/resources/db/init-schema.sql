CREATE SCHEMA IF NOT EXISTS mu_raw_lastfm;

-- Set search path for data management user
ALTER ROLE mu_raw_lastfm_dm SET search_path TO mu_raw_lastfm,public;

CREATE SCHEMA IF NOT EXISTS mu_raw_lastfm_staging;
GRANT ALL PRIVILEGES ON SCHEMA mu_raw_lastfm_staging TO mu_raw_lastfm_dm;
ALTER DEFAULT PRIVILEGES    IN SCHEMA mu_raw_lastfm_staging GRANT ALL ON TABLES     TO mu_raw_lastfm_dm;
ALTER DEFAULT PRIVILEGES    IN SCHEMA mu_raw_lastfm_staging GRANT ALL ON SEQUENCES  TO mu_raw_lastfm_dm;
ALTER DEFAULT PRIVILEGES    IN SCHEMA mu_raw_lastfm_staging GRANT ALL ON FUNCTIONS  TO mu_raw_lastfm_dm;

-- Note: Read-only user is not typically needed for tests as tests use the DM user
-- However, if you need to test with a read-only user, uncomment the following:
-- CREATE USER mu_raw_lastfm_reader WITH LOGIN PASSWORD 'test_password';
-- GRANT USAGE ON SCHEMA mu_raw_lastfm TO mu_raw_lastfm_reader;
-- GRANT USAGE ON SCHEMA mu_raw_lastfm_staging TO mu_raw_lastfm_reader;
-- GRANT SELECT ON ALL TABLES IN SCHEMA mu_raw_lastfm TO mu_raw_lastfm_reader;
-- GRANT SELECT ON ALL TABLES IN SCHEMA mu_raw_lastfm_staging TO mu_raw_lastfm_reader;
-- ALTER DEFAULT PRIVILEGES FOR ROLE mu_raw_lastfm_dm IN SCHEMA mu_raw_lastfm GRANT SELECT ON TABLES TO mu_raw_lastfm_reader;
-- ALTER DEFAULT PRIVILEGES FOR ROLE mu_raw_lastfm_dm IN SCHEMA mu_raw_lastfm_staging GRANT SELECT ON TABLES TO mu_raw_lastfm_reader;
-- ALTER DEFAULT PRIVILEGES FOR ROLE postgres IN SCHEMA mu_raw_lastfm GRANT SELECT ON TABLES TO mu_raw_lastfm_reader;
-- ALTER DEFAULT PRIVILEGES FOR ROLE postgres IN SCHEMA mu_raw_lastfm_staging GRANT SELECT ON TABLES TO mu_raw_lastfm_reader;
-- ALTER ROLE mu_raw_lastfm_reader SET search_path TO mu_raw_lastfm,mu_raw_lastfm_staging,public;