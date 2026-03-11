-- LastFM schemas and users
-- Grants and default privileges live in the lastfm Liquibase 0000-schema-grants migration

DO $mu_raw_lastfm_dm$
BEGIN
  IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'mu_raw_lastfm_dm') THEN
    CREATE USER mu_raw_lastfm_dm WITH LOGIN PASSWORD '${MURAW_LASTFM_DB_WRITER_PASSWORD}';
  END IF;
END;
$mu_raw_lastfm_dm$;

DO $mu_raw_lastfm_reader$
BEGIN
  IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'mu_raw_lastfm_reader') THEN
    CREATE USER mu_raw_lastfm_reader WITH LOGIN PASSWORD '${MURAW_LASTFM_DB_READER_PASSWORD}';
  END IF;
END;
$mu_raw_lastfm_reader$;

ALTER ROLE mu_raw_lastfm_dm     SET search_path TO mu_raw_lastfm, mu_raw_lastfm_staging, public;
ALTER ROLE mu_raw_lastfm_reader SET search_path TO mu_raw_lastfm, mu_raw_lastfm_staging, public;

CREATE SCHEMA IF NOT EXISTS mu_raw_lastfm         AUTHORIZATION mu_raw_lastfm_dm;
CREATE SCHEMA IF NOT EXISTS mu_raw_lastfm_staging AUTHORIZATION mu_raw_lastfm_dm;
