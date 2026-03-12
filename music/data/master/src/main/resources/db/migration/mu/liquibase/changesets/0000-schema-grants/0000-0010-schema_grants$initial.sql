-- Default privileges for mu_dm on mu and mu_view schemas.
-- Runs as mu_dm (the schema owner), so ALTER DEFAULT PRIVILEGES applies to objects
-- that mu_dm creates — i.e., all Liquibase-managed tables and views.

ALTER DEFAULT PRIVILEGES IN SCHEMA mu      GRANT ALL ON TABLES    TO mu_dm;
ALTER DEFAULT PRIVILEGES IN SCHEMA mu      GRANT ALL ON SEQUENCES TO mu_dm;
ALTER DEFAULT PRIVILEGES IN SCHEMA mu      GRANT ALL ON FUNCTIONS TO mu_dm;
ALTER DEFAULT PRIVILEGES IN SCHEMA mu_view GRANT ALL ON TABLES    TO mu_dm;
ALTER DEFAULT PRIVILEGES IN SCHEMA mu_view GRANT ALL ON SEQUENCES TO mu_dm;
ALTER DEFAULT PRIVILEGES IN SCHEMA mu_view GRANT ALL ON FUNCTIONS TO mu_dm;

-- Cross-schema: mu_quiz_dm reads from mu_view.
-- mu_dm is the schema owner and grantor; quiz module cannot issue this grant itself.
-- Guarded: mu_quiz_dm may not exist yet in test environments (no init.sh).
DO $cross_schema_mu_quiz_dm$
BEGIN
    IF EXISTS (SELECT FROM pg_roles WHERE rolname = 'mu_quiz_dm') THEN
        GRANT SELECT ON ALL TABLES IN SCHEMA mu_view TO mu_quiz_dm;
        ALTER DEFAULT PRIVILEGES IN SCHEMA mu_view GRANT SELECT ON TABLES TO mu_quiz_dm;
    END IF;
END;
$cross_schema_mu_quiz_dm$;
