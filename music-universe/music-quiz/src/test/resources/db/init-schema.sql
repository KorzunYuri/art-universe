-- =============================================================================
-- MU SCHEMA (Music Data)
-- =============================================================================
CREATE USER mu_dm WITH LOGIN PASSWORD 'mu_dm';
CREATE SCHEMA IF NOT EXISTS mu AUTHORIZATION mu_dm;
GRANT ALL PRIVILEGES ON SCHEMA mu TO mu_dm;
ALTER DEFAULT PRIVILEGES IN SCHEMA mu GRANT ALL ON TABLES TO mu_dm;
ALTER DEFAULT PRIVILEGES IN SCHEMA mu GRANT ALL ON SEQUENCES TO mu_dm;
ALTER DEFAULT PRIVILEGES IN SCHEMA mu GRANT ALL ON FUNCTIONS TO mu_dm;

-- =============================================================================
-- MU_VIEW SCHEMA (Read-only views for consumer modules)
-- =============================================================================
CREATE SCHEMA IF NOT EXISTS mu_view;
GRANT USAGE ON SCHEMA mu_view TO PUBLIC;
GRANT SELECT ON ALL TABLES IN SCHEMA mu_view TO PUBLIC;
ALTER DEFAULT PRIVILEGES IN SCHEMA mu_view GRANT SELECT ON TABLES TO PUBLIC;

-- =============================================================================
-- MU_QUIZ SCHEMA (Quiz data management)
-- =============================================================================
CREATE USER mu_quiz_dm WITH LOGIN PASSWORD 'mu_quiz_dm';
CREATE SCHEMA IF NOT EXISTS mu_quiz AUTHORIZATION mu_quiz_dm;
GRANT ALL PRIVILEGES ON SCHEMA mu_quiz TO mu_quiz_dm;
ALTER DEFAULT PRIVILEGES FOR ROLE mu_quiz_dm IN SCHEMA mu_quiz GRANT ALL ON TABLES TO mu_quiz_dm;
ALTER DEFAULT PRIVILEGES FOR ROLE mu_quiz_dm IN SCHEMA mu_quiz GRANT ALL ON SEQUENCES TO mu_quiz_dm;
ALTER DEFAULT PRIVILEGES FOR ROLE mu_quiz_dm IN SCHEMA mu_quiz GRANT ALL ON FUNCTIONS TO mu_quiz_dm;

-- =============================================================================
-- MU_QUIZ_STG SCHEMA (staging)
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

-- =============================================================================
-- TEST OBJECTS (mu_view)
-- =============================================================================
CREATE TABLE IF NOT EXISTS mu_view.v_track (
    id BIGINT PRIMARY KEY,
    primary_artist_id BIGINT NOT NULL,
    name VARCHAR(500)
);

CREATE TABLE IF NOT EXISTS mu_view.v_artist (
    id BIGINT PRIMARY KEY,
    name VARCHAR(500)
);

CREATE TABLE IF NOT EXISTS mu_view.v_artist_category (
    artist_id BIGINT,
    category_id BIGINT
);

CREATE TABLE IF NOT EXISTS mu_view.v_category_hierarchy (
    id BIGINT,
    parent_id BIGINT
);

-- =============================================================================
-- TEST-ONLY PERMISSIONS (NOT FOR PRODUCTION)
-- =============================================================================
-- Test permissions for mu_view schema
GRANT INSERT, DELETE, TRUNCATE ON ALL TABLES IN SCHEMA mu_view TO mu_quiz_dm;
ALTER DEFAULT PRIVILEGES IN SCHEMA mu_view GRANT INSERT, DELETE, TRUNCATE ON TABLES TO mu_quiz_dm;
