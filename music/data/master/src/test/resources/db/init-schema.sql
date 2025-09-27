CREATE SCHEMA IF NOT EXISTS mu;
CREATE SCHEMA IF NOT EXISTS mu_view;

-- Set search path for data management user
ALTER ROLE mu_dm SET search_path TO mu,public;

-- Grant public access to readonly schema
GRANT USAGE ON SCHEMA mu_view TO PUBLIC;
GRANT SELECT ON ALL TABLES IN SCHEMA mu_view TO PUBLIC;
ALTER DEFAULT PRIVILEGES IN SCHEMA mu_view GRANT SELECT ON TABLES TO PUBLIC;
