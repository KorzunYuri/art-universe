#!/bin/bash
# Music Data database initialization script
# Shared by Docker Compose and Kubernetes deployments
#
# Required environment variables:
#   POSTGRES_USER, POSTGRES_DB  - set by postgres image
#   MU_DATA_DB_PASSWORD_DM      - master data dm user password
#   MU_QUIZ_DB_PASSWORD_DM      - quiz dm user password
#   ART_DATA_DB_PASSWORD_DM     - art data dm user password

set -e

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    -- Create database
    CREATE DATABASE music_universe;
EOSQL

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "music_universe" <<-EOSQL
    -- =============================================================================
    -- MASTER DATA SCHEMA
    -- =============================================================================
    CREATE USER mu_dm WITH LOGIN PASSWORD '$MU_DATA_DB_PASSWORD_DM';
    GRANT CREATE ON DATABASE music_universe TO mu_dm;
    CREATE SCHEMA IF NOT EXISTS mu AUTHORIZATION mu_dm;
    GRANT ALL PRIVILEGES ON SCHEMA mu TO mu_dm;
    ALTER DEFAULT PRIVILEGES IN SCHEMA mu GRANT ALL ON TABLES TO mu_dm;
    ALTER DEFAULT PRIVILEGES IN SCHEMA mu GRANT ALL ON SEQUENCES TO mu_dm;
    ALTER DEFAULT PRIVILEGES IN SCHEMA mu GRANT ALL ON FUNCTIONS TO mu_dm;

    -- =============================================================================
    -- MU VIEW SCHEMA (for cross-schema reads)
    -- =============================================================================
    CREATE SCHEMA IF NOT EXISTS mu_view AUTHORIZATION mu_dm;
    GRANT ALL PRIVILEGES ON SCHEMA mu_view TO mu_dm;
    ALTER DEFAULT PRIVILEGES IN SCHEMA mu_view GRANT ALL ON TABLES TO mu_dm;
    ALTER DEFAULT PRIVILEGES IN SCHEMA mu_view GRANT ALL ON SEQUENCES TO mu_dm;
    ALTER DEFAULT PRIVILEGES IN SCHEMA mu_view GRANT ALL ON FUNCTIONS TO mu_dm;

    -- =============================================================================
    -- QUIZ DATA SCHEMA
    -- =============================================================================
    CREATE USER mu_quiz_dm WITH LOGIN PASSWORD '$MU_QUIZ_DB_PASSWORD_DM';
    GRANT CREATE ON DATABASE music_universe TO mu_quiz_dm;
    CREATE SCHEMA IF NOT EXISTS mu_quiz AUTHORIZATION mu_quiz_dm;
    GRANT ALL PRIVILEGES ON SCHEMA mu_quiz TO mu_quiz_dm;
    ALTER DEFAULT PRIVILEGES IN SCHEMA mu_quiz GRANT ALL ON TABLES TO mu_quiz_dm;
    ALTER DEFAULT PRIVILEGES IN SCHEMA mu_quiz GRANT ALL ON SEQUENCES TO mu_quiz_dm;
    ALTER DEFAULT PRIVILEGES IN SCHEMA mu_quiz GRANT ALL ON FUNCTIONS TO mu_quiz_dm;

    -- =============================================================================
    -- QUIZ STAGING SCHEMA
    -- =============================================================================
    CREATE SCHEMA IF NOT EXISTS mu_quiz_stg;
    GRANT ALL PRIVILEGES ON SCHEMA mu_quiz_stg TO mu_quiz_dm;
    ALTER DEFAULT PRIVILEGES IN SCHEMA mu_quiz_stg GRANT ALL ON TABLES TO mu_quiz_dm;
    ALTER DEFAULT PRIVILEGES IN SCHEMA mu_quiz_stg GRANT ALL ON SEQUENCES TO mu_quiz_dm;
    ALTER DEFAULT PRIVILEGES IN SCHEMA mu_quiz_stg GRANT ALL ON FUNCTIONS TO mu_quiz_dm;

    -- =============================================================================
    -- ART FOUNDATION SCHEMA
    -- =============================================================================
    CREATE USER art_dm WITH LOGIN PASSWORD '$ART_DATA_DB_PASSWORD_DM';
    GRANT CREATE ON DATABASE music_universe TO art_dm;
    CREATE SCHEMA IF NOT EXISTS art AUTHORIZATION art_dm;
    GRANT ALL PRIVILEGES ON SCHEMA art TO art_dm;
    ALTER DEFAULT PRIVILEGES IN SCHEMA art GRANT ALL ON TABLES TO art_dm;
    ALTER DEFAULT PRIVILEGES IN SCHEMA art GRANT ALL ON SEQUENCES TO art_dm;
    ALTER DEFAULT PRIVILEGES IN SCHEMA art GRANT ALL ON FUNCTIONS TO art_dm;

    -- =============================================================================
    -- ART VIEW SCHEMA (for cross-schema reads from art)
    -- =============================================================================
    CREATE SCHEMA IF NOT EXISTS art_view AUTHORIZATION art_dm;
    GRANT ALL PRIVILEGES ON SCHEMA art_view TO art_dm;
    ALTER DEFAULT PRIVILEGES IN SCHEMA art_view GRANT ALL ON TABLES TO art_dm;
    ALTER DEFAULT PRIVILEGES IN SCHEMA art_view GRANT ALL ON SEQUENCES TO art_dm;
    ALTER DEFAULT PRIVILEGES IN SCHEMA art_view GRANT ALL ON FUNCTIONS TO art_dm;

    -- =============================================================================
    -- Cross-schema read access
    -- =============================================================================
    -- mu_quiz_dm: read from mu_view
    GRANT USAGE ON SCHEMA mu_view TO mu_quiz_dm;
    GRANT SELECT ON ALL TABLES IN SCHEMA mu_view TO mu_quiz_dm;
    ALTER DEFAULT PRIVILEGES IN SCHEMA mu_view GRANT SELECT ON TABLES TO mu_quiz_dm;

    -- mu_dm: read from art_view
    GRANT USAGE ON SCHEMA art_view TO mu_dm;
    GRANT SELECT ON ALL TABLES IN SCHEMA art_view TO mu_dm;
    ALTER DEFAULT PRIVILEGES IN SCHEMA art_view GRANT SELECT ON TABLES TO mu_dm;
EOSQL
