-- Spotify shared schemas and dm/reader users
-- Grants and default privileges live in the spotify Liquibase 0000-schema-grants migration

DO $mu_raw_spotify_dm$
BEGIN
  IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'mu_raw_spotify_dm') THEN
    CREATE USER mu_raw_spotify_dm WITH LOGIN PASSWORD '${MURAW_SPOTIFY_DB_WRITER_PASSWORD}';
  END IF;
END;
$mu_raw_spotify_dm$;

DO $mu_raw_spotify_reader$
BEGIN
  IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'mu_raw_spotify_reader') THEN
    CREATE USER mu_raw_spotify_reader WITH LOGIN PASSWORD '${MURAW_SPOTIFY_DB_READER_PASSWORD}';
  END IF;
END;
$mu_raw_spotify_reader$;

ALTER ROLE mu_raw_spotify_dm     SET search_path TO mu_raw_spotify, mu_raw_spotify_staging, public;
ALTER ROLE mu_raw_spotify_reader SET search_path TO mu_raw_spotify, mu_raw_spotify_staging, public;

GRANT CREATE ON DATABASE art_universe TO mu_raw_spotify_dm;

CREATE SCHEMA IF NOT EXISTS mu_raw_spotify         AUTHORIZATION mu_raw_spotify_dm;
CREATE SCHEMA IF NOT EXISTS mu_raw_spotify_staging AUTHORIZATION mu_raw_spotify_dm;
