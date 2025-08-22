CREATE SCHEMA IF NOT EXISTS mu_raw_lastfm;

-- Set search path for data management user
ALTER ROLE mu_raw_lastfm_dm SET search_path TO mu_raw_lastfm,public;
