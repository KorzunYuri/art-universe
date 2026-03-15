-- au_config schema and dm user
-- Grants and default privileges live in the config-service Liquibase 0000-schema-grants migration

DO $au_config_dm$
BEGIN
  IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'au_config_dm') THEN
    CREATE USER au_config_dm WITH LOGIN PASSWORD '${AU_CONFIG_DB_PASSWORD_DM}';
  END IF;
END;
$au_config_dm$;

GRANT CREATE ON DATABASE art_universe TO au_config_dm;

CREATE SCHEMA IF NOT EXISTS au_config AUTHORIZATION au_config_dm;
ALTER ROLE au_config_dm SET search_path TO au_config, public;
