-- Spotify ETL: calls-generator user
-- Schema grants live in the spotify Liquibase 0000-schema-grants migration
-- Table-level grants live in the spotify Liquibase 0006-module-user-grants migration

DO $mu_raw_spotify_gen$
BEGIN
  IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'mu_raw_spotify_gen') THEN
    CREATE USER mu_raw_spotify_gen WITH LOGIN PASSWORD '${MURAW_SPOTIFY_CALLS_GENERATOR_DB_PASSWORD}';
  END IF;
END;
$mu_raw_spotify_gen$;

ALTER ROLE mu_raw_spotify_gen SET search_path TO mu_raw_spotify, public;
