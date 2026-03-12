-- Quiz schemas and user
-- Grants and default privileges live in the quiz Liquibase 0000-schema-grants migration

DO $mu_quiz_dm$
BEGIN
  IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'mu_quiz_dm') THEN
    CREATE USER mu_quiz_dm WITH LOGIN PASSWORD '${MU_QUIZ_DB_PASSWORD_DM}';
  END IF;
END;
$mu_quiz_dm$;

GRANT CREATE ON DATABASE art_universe TO mu_quiz_dm;

CREATE SCHEMA IF NOT EXISTS mu_quiz     AUTHORIZATION mu_quiz_dm;
CREATE SCHEMA IF NOT EXISTS mu_quiz_stg AUTHORIZATION mu_quiz_dm;
