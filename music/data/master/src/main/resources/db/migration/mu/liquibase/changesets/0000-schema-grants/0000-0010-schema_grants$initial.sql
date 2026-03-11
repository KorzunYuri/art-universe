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
GRANT SELECT ON ALL TABLES IN SCHEMA mu_view TO mu_quiz_dm;
ALTER DEFAULT PRIVILEGES IN SCHEMA mu_view GRANT SELECT ON TABLES TO mu_quiz_dm;
