CREATE SCHEMA IF NOT EXISTS mu_quiz;

-- Set search path for the user
ALTER ROLE mu_quiz_dm SET search_path TO mu_quiz,public;
