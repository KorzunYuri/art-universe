-- Default privileges for mu_raw_spotify_dm on the mu_raw_spotify schema.
-- Grants read access to mu_raw_spotify_reader and schema-level access to ETL users.
-- Runs as mu_raw_spotify_dm (owner of mu_raw_spotify).
--
-- Note: mu_raw_spotify_staging is owned by mu_raw_spotify_appl (set by init.sh).
-- All staging schema grants are therefore in init.sh 0011-spotify-etl-appl.sql,
-- where they run as postgres (superuser).

ALTER DEFAULT PRIVILEGES IN SCHEMA mu_raw_spotify GRANT ALL ON TABLES    TO mu_raw_spotify_dm;
ALTER DEFAULT PRIVILEGES IN SCHEMA mu_raw_spotify GRANT ALL ON SEQUENCES TO mu_raw_spotify_dm;
ALTER DEFAULT PRIVILEGES IN SCHEMA mu_raw_spotify GRANT ALL ON FUNCTIONS TO mu_raw_spotify_dm;

-- Reader: schema usage + existing tables + future tables in mu_raw_spotify only
GRANT USAGE ON SCHEMA mu_raw_spotify TO mu_raw_spotify_reader;
GRANT SELECT ON ALL TABLES IN SCHEMA mu_raw_spotify TO mu_raw_spotify_reader;
ALTER DEFAULT PRIVILEGES IN SCHEMA mu_raw_spotify GRANT SELECT ON TABLES TO mu_raw_spotify_reader;

-- ETL users: schema USAGE and SELECT on all (existing + future) tables in mu_raw_spotify.
-- Fine-grained INSERT/UPDATE grants are in 0006-module-user-grants.
GRANT USAGE ON SCHEMA mu_raw_spotify
    TO mu_raw_spotify_gen, mu_raw_spotify_perf, mu_raw_spotify_parser, mu_raw_spotify_appl;

GRANT SELECT ON ALL TABLES IN SCHEMA mu_raw_spotify
    TO mu_raw_spotify_gen, mu_raw_spotify_perf, mu_raw_spotify_parser, mu_raw_spotify_appl;

GRANT USAGE ON ALL SEQUENCES IN SCHEMA mu_raw_spotify
    TO mu_raw_spotify_gen, mu_raw_spotify_perf, mu_raw_spotify_parser, mu_raw_spotify_appl;

ALTER DEFAULT PRIVILEGES IN SCHEMA mu_raw_spotify GRANT SELECT ON TABLES    TO mu_raw_spotify_gen;
ALTER DEFAULT PRIVILEGES IN SCHEMA mu_raw_spotify GRANT SELECT ON TABLES    TO mu_raw_spotify_perf;
ALTER DEFAULT PRIVILEGES IN SCHEMA mu_raw_spotify GRANT SELECT ON TABLES    TO mu_raw_spotify_parser;
ALTER DEFAULT PRIVILEGES IN SCHEMA mu_raw_spotify GRANT SELECT ON TABLES    TO mu_raw_spotify_appl;
ALTER DEFAULT PRIVILEGES IN SCHEMA mu_raw_spotify GRANT USAGE  ON SEQUENCES TO mu_raw_spotify_gen;
ALTER DEFAULT PRIVILEGES IN SCHEMA mu_raw_spotify GRANT USAGE  ON SEQUENCES TO mu_raw_spotify_perf;
ALTER DEFAULT PRIVILEGES IN SCHEMA mu_raw_spotify GRANT USAGE  ON SEQUENCES TO mu_raw_spotify_parser;
ALTER DEFAULT PRIVILEGES IN SCHEMA mu_raw_spotify GRANT USAGE  ON SEQUENCES TO mu_raw_spotify_appl;

-- mu_view: schema USAGE for generator (SELECT on individual views is granted by
-- music-master Liquibase migration 0022-0020-spotify_generator_grants)
GRANT USAGE ON SCHEMA mu_view TO mu_raw_spotify_gen;
