CREATE SCHEMA IF NOT EXISTS mu_raw_lastfm;

-- Set search path for data management user
ALTER ROLE mu_raw_lastfm_dm SET search_path TO mu_raw_lastfm,public;

CREATE SCHEMA IF NOT EXISTS mu_raw_lastfm_staging;
GRANT ALL PRIVILEGES ON SCHEMA mu_raw_lastfm_staging TO mu_raw_lastfm_dm;
ALTER DEFAULT PRIVILEGES    IN SCHEMA mu_raw_lastfm_staging GRANT ALL ON TABLES     TO mu_raw_lastfm_dm;
ALTER DEFAULT PRIVILEGES    IN SCHEMA mu_raw_lastfm_staging GRANT ALL ON SEQUENCES  TO mu_raw_lastfm_dm;
ALTER DEFAULT PRIVILEGES    IN SCHEMA mu_raw_lastfm_staging GRANT ALL ON FUNCTIONS  TO mu_raw_lastfm_dm;