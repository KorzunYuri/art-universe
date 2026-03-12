-- =============================================================================
-- Stubs for mu_view objects owned by music-data-master module.
-- These exist only in test environments so that quiz Liquibase migrations
-- (which reference mu_view views) can resolve successfully.
-- =============================================================================

CREATE TABLE IF NOT EXISTS mu_view.v_track (
    id                BIGINT PRIMARY KEY,
    primary_artist_id BIGINT NOT NULL,
    name              VARCHAR(500)
);

CREATE TABLE IF NOT EXISTS mu_view.v_artist (
    id   BIGINT PRIMARY KEY,
    name VARCHAR(500)
);

CREATE TABLE IF NOT EXISTS mu_view.v_artist_category (
    artist_id   BIGINT,
    category_id BIGINT
);

CREATE TABLE IF NOT EXISTS mu_view.v_category_children (
    id         BIGINT,
    name       VARCHAR(500),
    child_id   BIGINT,
    child_name VARCHAR(500)
);

-- Read access for mu_quiz_dm on mu_view (normally set by master-data Liquibase, absent in quiz-only tests)
GRANT SELECT ON ALL TABLES IN SCHEMA mu_view TO mu_quiz_dm;
ALTER DEFAULT PRIVILEGES FOR ROLE mu_dm IN SCHEMA mu_view GRANT SELECT ON TABLES TO mu_quiz_dm;

-- Write access needed by quiz tests to seed/clean fixture data
GRANT INSERT, DELETE, TRUNCATE ON ALL TABLES IN SCHEMA mu_view TO mu_quiz_dm;
ALTER DEFAULT PRIVILEGES FOR ROLE mu_dm IN SCHEMA mu_view GRANT INSERT, DELETE, TRUNCATE ON TABLES TO mu_quiz_dm;
