CREATE DATABASE music_universe;
\connect music_universe;


-- =============================================================================
-- MASTER DATA SCHEMA
-- =============================================================================
CREATE USER mu_dm WITH LOGIN PASSWORD '12345';
CREATE SCHEMA IF NOT EXISTS mu AUTHORIZATION mu_dm;
GRANT ALL PRIVILEGES ON SCHEMA mu TO mu_dm;
ALTER DEFAULT PRIVILEGES IN SCHEMA mu GRANT ALL ON TABLES TO mu_dm;
ALTER DEFAULT PRIVILEGES IN SCHEMA mu GRANT ALL ON SEQUENCES TO mu_dm;
ALTER DEFAULT PRIVILEGES IN SCHEMA mu GRANT ALL ON FUNCTIONS TO mu_dm;


-- =============================================================================
-- QUIZ DATA SCHEMA
-- =============================================================================
CREATE USER mu_quiz_dm WITH LOGIN PASSWORD '12345';
CREATE SCHEMA IF NOT EXISTS mu_quiz AUTHORIZATION mu_quiz_dm;
GRANT ALL PRIVILEGES ON SCHEMA mu_quiz TO mu_quiz_dm;
ALTER DEFAULT PRIVILEGES IN SCHEMA mu_quiz GRANT ALL ON TABLES TO mu_quiz_dm;
ALTER DEFAULT PRIVILEGES IN SCHEMA mu_quiz GRANT ALL ON SEQUENCES TO mu_quiz_dm;
ALTER DEFAULT PRIVILEGES IN SCHEMA mu_quiz GRANT ALL ON FUNCTIONS TO mu_quiz_dm;


-- =============================================================================
-- QUIZ STAGING SCHEMA
-- =============================================================================
CREATE SCHEMA IF NOT EXISTS mu_quiz_stg;
GRANT ALL PRIVILEGES ON SCHEMA mu_quiz_stg TO mu_quiz_dm;
ALTER DEFAULT PRIVILEGES IN SCHEMA mu_quiz_stg GRANT ALL ON TABLES TO mu_quiz_dm;
ALTER DEFAULT PRIVILEGES IN SCHEMA mu_quiz_stg GRANT ALL ON SEQUENCES TO mu_quiz_dm;
ALTER DEFAULT PRIVILEGES IN SCHEMA mu_quiz_stg GRANT ALL ON FUNCTIONS TO mu_quiz_dm;

-- =============================================================================
-- Cross-schema read access
-- =============================================================================
-- mu_quiz_dm: читать mu_view
GRANT USAGE ON SCHEMA mu_view TO mu_quiz_dm;
GRANT SELECT ON ALL TABLES IN SCHEMA mu_view TO mu_quiz_dm;
ALTER DEFAULT PRIVILEGES IN SCHEMA mu_view GRANT SELECT ON TABLES TO mu_quiz_dm;