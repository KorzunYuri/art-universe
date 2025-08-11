-- Create readonly schema for consumer modules
CREATE SCHEMA IF NOT EXISTS mu_view;

-- Grant usage on schema to all users
GRANT USAGE ON SCHEMA mu_view TO PUBLIC;
GRANT SELECT ON ALL TABLES IN SCHEMA mu_view TO PUBLIC;
ALTER DEFAULT PRIVILEGES IN SCHEMA mu_view GRANT SELECT ON TABLES TO PUBLIC;
