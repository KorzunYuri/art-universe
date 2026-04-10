-- Grant mu_raw_spotify_gen (Spotify calls-generator) read access to the entire
-- mu_view schema, including objects that will be created in the future.

-- Schema usage (idempotent; also set in 01-init.sh)
GRANT USAGE ON SCHEMA mu_view TO mu_raw_spotify_gen;

-- SELECT on all views currently in mu_view
GRANT SELECT ON ALL TABLES IN SCHEMA mu_view TO mu_raw_spotify_gen;

-- SELECT on all future views/tables created by mu_dm in mu_view.
-- This migration runs as mu_dm, so no FOR ROLE clause is needed.
ALTER DEFAULT PRIVILEGES IN SCHEMA mu_view
    GRANT SELECT ON TABLES TO mu_raw_spotify_gen;
