-- Read-only MCP user for art-universe-mcp-server
-- Has USAGE + SELECT on all schemas for database exploration

DO $mcp_user$
BEGIN
  IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'mu_mcp_user') THEN
    CREATE USER mu_mcp_user WITH LOGIN PASSWORD '${MCP_USER_DB_PASSWORD}';
  END IF;
END;
$mcp_user$;

-- Grant USAGE and SELECT on all existing schemas
GRANT USAGE ON SCHEMA mu_raw_lastfm, mu_raw_lastfm_staging, mu_raw_spotify, mu_raw_spotify_staging,
                      mu, mu_view, mu_quiz, mu_quiz_stg, art, art_view, au_config, au_auth TO mu_mcp_user;

GRANT SELECT ON ALL TABLES IN SCHEMA mu_raw_lastfm, mu_raw_lastfm_staging, mu_raw_spotify, mu_raw_spotify_staging,
                                     mu, mu_view, mu_quiz, mu_quiz_stg, art, art_view, au_config, au_auth TO mu_mcp_user;

-- Ensure future tables are also readable
ALTER DEFAULT PRIVILEGES IN SCHEMA mu_raw_lastfm, mu_raw_lastfm_staging, mu_raw_spotify, mu_raw_spotify_staging,
                                   mu, mu_view, mu_quiz, mu_quiz_stg, art, art_view, au_config, au_auth
  GRANT SELECT ON TABLES TO mu_mcp_user;
