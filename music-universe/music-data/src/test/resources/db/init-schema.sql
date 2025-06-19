CREATE SCHEMA IF NOT EXISTS mu;

-- Set search path for the user
ALTER ROLE mu_dm SET search_path TO mu,public;
