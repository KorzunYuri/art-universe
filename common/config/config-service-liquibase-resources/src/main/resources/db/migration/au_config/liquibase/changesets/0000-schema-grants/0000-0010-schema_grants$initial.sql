-- Default privileges for au_config_dm on au_config schema.
-- Runs as au_config_dm (the schema owner).

ALTER DEFAULT PRIVILEGES IN SCHEMA au_config GRANT ALL ON TABLES    TO au_config_dm;
ALTER DEFAULT PRIVILEGES IN SCHEMA au_config GRANT ALL ON SEQUENCES TO au_config_dm;
ALTER DEFAULT PRIVILEGES IN SCHEMA au_config GRANT ALL ON FUNCTIONS TO au_config_dm;
