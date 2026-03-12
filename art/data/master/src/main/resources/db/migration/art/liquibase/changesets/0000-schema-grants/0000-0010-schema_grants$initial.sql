-- Default privileges for art_dm on art and art_view schemas.
-- Also grants read access to mu_dm on art_view (cross-schema).
-- Runs as art_dm (the schema owner).

ALTER DEFAULT PRIVILEGES IN SCHEMA art      GRANT ALL ON TABLES    TO art_dm;
ALTER DEFAULT PRIVILEGES IN SCHEMA art      GRANT ALL ON SEQUENCES TO art_dm;
ALTER DEFAULT PRIVILEGES IN SCHEMA art      GRANT ALL ON FUNCTIONS TO art_dm;
ALTER DEFAULT PRIVILEGES IN SCHEMA art_view GRANT ALL ON TABLES    TO art_dm;
ALTER DEFAULT PRIVILEGES IN SCHEMA art_view GRANT ALL ON SEQUENCES TO art_dm;
ALTER DEFAULT PRIVILEGES IN SCHEMA art_view GRANT ALL ON FUNCTIONS TO art_dm;

-- Cross-schema: mu_dm reads from art_view.
-- art_dm is the schema owner and grantor; music-master module cannot issue this grant itself.
-- Guarded: mu_dm may not exist yet in test environments (no init.sh).
DO $cross_schema_mu_dm$
BEGIN
    IF EXISTS (SELECT FROM pg_roles WHERE rolname = 'mu_dm') THEN
        GRANT USAGE ON SCHEMA art_view TO mu_dm;
        GRANT SELECT ON ALL TABLES IN SCHEMA art_view TO mu_dm;
        ALTER DEFAULT PRIVILEGES IN SCHEMA art_view GRANT SELECT ON TABLES TO mu_dm;
    END IF;
END;
$cross_schema_mu_dm$;
