-- Art schemas and user
-- Grants and default privileges live in the art Liquibase 0000-schema-grants migration

DO $art_dm$
BEGIN
  IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'art_dm') THEN
    CREATE USER art_dm WITH LOGIN PASSWORD '${ART_DATA_DB_PASSWORD_DM}';
  END IF;
END;
$art_dm$;

GRANT CREATE ON DATABASE art_universe TO art_dm;

CREATE SCHEMA IF NOT EXISTS art      AUTHORIZATION art_dm;
CREATE SCHEMA IF NOT EXISTS art_view AUTHORIZATION art_dm;
