-- au_auth schema and dm user
-- Grants and default privileges live in the auth-service Liquibase 0000-schema-grants migration

DO $au_auth_dm$
BEGIN
  IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'au_auth_dm') THEN
    CREATE USER au_auth_dm WITH LOGIN PASSWORD '${AU_AUTH_DB_PASSWORD_DM}';
  END IF;
END;
$au_auth_dm$;

GRANT CREATE ON DATABASE art_universe TO au_auth_dm;

CREATE SCHEMA IF NOT EXISTS au_auth AUTHORIZATION au_auth_dm;
ALTER ROLE au_auth_dm SET search_path TO au_auth, public;
