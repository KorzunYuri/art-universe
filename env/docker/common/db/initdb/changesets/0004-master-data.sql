-- Master data (mu) schemas and user
-- Grants and default privileges live in the music-master Liquibase 0000-schema-grants migration

DO $mu_dm$
BEGIN
  IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'mu_dm') THEN
    CREATE USER mu_dm WITH LOGIN PASSWORD '${MU_DATA_DB_PASSWORD_DM}';
  END IF;
END;
$mu_dm$;

-- CREATE privilege needed for Liquibase to create new schemas on future module additions
GRANT CREATE ON DATABASE art_universe TO mu_dm;

CREATE SCHEMA IF NOT EXISTS mu      AUTHORIZATION mu_dm;
CREATE SCHEMA IF NOT EXISTS mu_view AUTHORIZATION mu_dm;

-- Public USAGE on mu_view is set here (not in Liquibase) so that it is in place
-- before any other module starts up and tries to use mu_view views.
GRANT USAGE ON SCHEMA mu_view TO PUBLIC;
