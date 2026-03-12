-- Spotify ETL: staging-applicator user
-- Schema grants live in the spotify Liquibase 0000-schema-grants migration
-- Table-level grants live in the spotify Liquibase 0006-module-user-grants migration

DO $mu_raw_spotify_appl$
BEGIN
  IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'mu_raw_spotify_appl') THEN
    CREATE USER mu_raw_spotify_appl WITH LOGIN PASSWORD '${MURAW_SPOTIFY_STAGING_APPLICATOR_DB_PASSWORD}';
  END IF;
END;
$mu_raw_spotify_appl$;

ALTER ROLE mu_raw_spotify_appl SET search_path TO mu_raw_spotify, mu_raw_spotify_staging, public;

-- Staging schema: applicator takes ownership so it can DROP parser-created runtime stg_*_N tables.
-- Schema ownership is the mechanism — no per-table grants needed for DROP.
ALTER SCHEMA mu_raw_spotify_staging OWNER TO mu_raw_spotify_appl;

-- Parser needs CREATE + USAGE in staging to create runtime stg_*_N tables.
-- Granted here (after appl takes ownership) so the schema owner is the grantor.
GRANT USAGE, CREATE ON SCHEMA mu_raw_spotify_staging TO mu_raw_spotify_parser;

-- Applicator needs to SELECT and UPDATE stg_*_N tables that parser creates.
-- ALTER DEFAULT PRIVILEGES FOR ROLE <other> requires superuser — hence in init.sh.
ALTER DEFAULT PRIVILEGES FOR ROLE mu_raw_spotify_parser IN SCHEMA mu_raw_spotify_staging
    GRANT SELECT, INSERT, UPDATE ON TABLES TO mu_raw_spotify_appl;

-- Reader: read access to staging (optional, for monitoring/debugging).
GRANT USAGE ON SCHEMA mu_raw_spotify_staging TO mu_raw_spotify_reader;
ALTER DEFAULT PRIVILEGES FOR ROLE mu_raw_spotify_parser IN SCHEMA mu_raw_spotify_staging
    GRANT SELECT ON TABLES TO mu_raw_spotify_reader;
