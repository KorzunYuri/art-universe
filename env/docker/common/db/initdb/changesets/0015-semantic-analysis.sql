-- Semantic analysis / proposals schema and user

DO $mu_sa_dm$
BEGIN
  IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'mu_sa_dm') THEN
    CREATE USER mu_sa_dm WITH LOGIN PASSWORD '${MU_SA_DB_PASSWORD_DM}';
  END IF;
END;
$mu_sa_dm$;

CREATE SCHEMA IF NOT EXISTS mu_semantic_analysis AUTHORIZATION mu_sa_dm;

-- mu_sa_dm: full access to mu_semantic_analysis
GRANT ALL PRIVILEGES ON SCHEMA mu_semantic_analysis TO mu_sa_dm;
ALTER DEFAULT PRIVILEGES IN SCHEMA mu_semantic_analysis GRANT ALL ON TABLES TO mu_sa_dm;
ALTER DEFAULT PRIVILEGES IN SCHEMA mu_semantic_analysis GRANT ALL ON SEQUENCES TO mu_sa_dm;
ALTER DEFAULT PRIVILEGES IN SCHEMA mu_semantic_analysis GRANT ALL ON FUNCTIONS TO mu_sa_dm;

-- mu_sa_dm: read access to mu schema (for category tree, entity matching)
GRANT USAGE ON SCHEMA mu TO mu_sa_dm;
GRANT SELECT ON ALL TABLES IN SCHEMA mu TO mu_sa_dm;
ALTER DEFAULT PRIVILEGES IN SCHEMA mu GRANT SELECT ON TABLES TO mu_sa_dm;

-- mu_raw_lastfm_reader: read access to mu_semantic_analysis (for trigger dedup queries)
GRANT USAGE ON SCHEMA mu_semantic_analysis TO mu_raw_lastfm_reader;
GRANT SELECT ON ALL TABLES IN SCHEMA mu_semantic_analysis TO mu_raw_lastfm_reader;
ALTER DEFAULT PRIVILEGES IN SCHEMA mu_semantic_analysis GRANT SELECT ON TABLES TO mu_raw_lastfm_reader;

-- mu_dm: read/write access to mu_semantic_analysis schema (for proposal review API)
GRANT USAGE ON SCHEMA mu_semantic_analysis TO mu_dm;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA mu_semantic_analysis TO mu_dm;
GRANT USAGE ON ALL SEQUENCES IN SCHEMA mu_semantic_analysis TO mu_dm;
ALTER DEFAULT PRIVILEGES IN SCHEMA mu_semantic_analysis GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO mu_dm;
ALTER DEFAULT PRIVILEGES IN SCHEMA mu_semantic_analysis GRANT USAGE ON SEQUENCES TO mu_dm;
