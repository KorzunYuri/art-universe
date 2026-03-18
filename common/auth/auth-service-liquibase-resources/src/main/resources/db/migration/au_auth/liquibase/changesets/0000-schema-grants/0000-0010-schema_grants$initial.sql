-- Default privileges for au_auth_dm on au_auth schema.
-- Runs as au_auth_dm (the schema owner).

ALTER DEFAULT PRIVILEGES IN SCHEMA au_auth GRANT ALL ON TABLES    TO au_auth_dm;
ALTER DEFAULT PRIVILEGES IN SCHEMA au_auth GRANT ALL ON SEQUENCES TO au_auth_dm;
ALTER DEFAULT PRIVILEGES IN SCHEMA au_auth GRANT ALL ON FUNCTIONS TO au_auth_dm;
