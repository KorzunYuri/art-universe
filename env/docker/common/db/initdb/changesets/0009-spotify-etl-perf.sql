-- Spotify ETL: calls-performer user
-- Schema grants live in the spotify Liquibase 0000-schema-grants migration
-- Table-level grants live in the spotify Liquibase 0006-module-user-grants migration

DO $mu_raw_spotify_perf$
BEGIN
  IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'mu_raw_spotify_perf') THEN
    CREATE USER mu_raw_spotify_perf WITH LOGIN PASSWORD '${MURAW_SPOTIFY_CALLS_PERFORMER_DB_PASSWORD}';
  END IF;
END;
$mu_raw_spotify_perf$;

ALTER ROLE mu_raw_spotify_perf SET search_path TO mu_raw_spotify, public;
