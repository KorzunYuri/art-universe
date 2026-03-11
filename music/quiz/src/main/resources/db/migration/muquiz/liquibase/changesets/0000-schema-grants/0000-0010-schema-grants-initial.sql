-- Default privileges for mu_quiz_dm on mu_quiz and mu_quiz_stg schemas.
-- Runs as mu_quiz_dm (the schema owner).
-- Cross-schema read access on mu_view is granted from the music-master module
-- (0000-schema-grants) because mu_dm, not mu_quiz_dm, owns mu_view.

ALTER DEFAULT PRIVILEGES IN SCHEMA mu_quiz     GRANT ALL ON TABLES    TO mu_quiz_dm;
ALTER DEFAULT PRIVILEGES IN SCHEMA mu_quiz     GRANT ALL ON SEQUENCES TO mu_quiz_dm;
ALTER DEFAULT PRIVILEGES IN SCHEMA mu_quiz     GRANT ALL ON FUNCTIONS TO mu_quiz_dm;
ALTER DEFAULT PRIVILEGES IN SCHEMA mu_quiz_stg GRANT ALL ON TABLES    TO mu_quiz_dm;
ALTER DEFAULT PRIVILEGES IN SCHEMA mu_quiz_stg GRANT ALL ON SEQUENCES TO mu_quiz_dm;
ALTER DEFAULT PRIVILEGES IN SCHEMA mu_quiz_stg GRANT ALL ON FUNCTIONS TO mu_quiz_dm;
