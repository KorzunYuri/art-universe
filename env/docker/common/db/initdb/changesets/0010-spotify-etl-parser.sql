-- Spotify ETL: response-parser user
-- Schema grants live in the spotify Liquibase 0000-schema-grants migration
-- Table-level grants live in the spotify Liquibase 0006-module-user-grants migration

DO $mu_raw_spotify_parser$
BEGIN
  IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'mu_raw_spotify_parser') THEN
    CREATE USER mu_raw_spotify_parser WITH LOGIN PASSWORD '${MURAW_SPOTIFY_RESPONSE_PARSER_DB_PASSWORD}';
  END IF;
END;
$mu_raw_spotify_parser$;

ALTER ROLE mu_raw_spotify_parser SET search_path TO mu_raw_spotify, mu_raw_spotify_staging, public;
