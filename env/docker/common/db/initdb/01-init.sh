#!/bin/bash
# Art Universe - Unified database initialization script
# Shared by Docker Compose and Kubernetes deployments
#
# Creates a single 'art_universe' database with all schemas, users, and permissions.
# Configures streaming replication for master-replica setup.
#
# Required environment variables:
#   POSTGRES_USER, POSTGRES_DB              - set by postgres image
#   MURAW_LASTFM_DB_WRITER_PASSWORD         - lastfm dm user password
#   MURAW_LASTFM_DB_READER_PASSWORD         - lastfm reader user password
#   MURAW_LASTFM_DB_REPLICATION_PASSWORD    - replication user password
#   MU_DATA_DB_PASSWORD_DM                  - master data dm user password
#   MU_QUIZ_DB_PASSWORD_DM                  - quiz dm user password
#   ART_DATA_DB_PASSWORD_DM                 - art data dm user password
#   PGDATA                                  - postgres data directory (set by image)

set -e

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    -- Create database
    CREATE DATABASE art_universe;
EOSQL

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "art_universe" <<-EOSQL
    -- =============================================================================
    -- USERS
    -- =============================================================================

    -- LastFM data management user (write access)
    CREATE USER mu_raw_lastfm_dm WITH LOGIN PASSWORD '$MURAW_LASTFM_DB_WRITER_PASSWORD';

    -- LastFM read-only user (for replica reads)
    CREATE USER mu_raw_lastfm_reader WITH LOGIN PASSWORD '$MURAW_LASTFM_DB_READER_PASSWORD';

    -- Replication user for streaming replication
    CREATE USER replicator WITH REPLICATION LOGIN PASSWORD '$MURAW_LASTFM_DB_REPLICATION_PASSWORD';

    -- Music master data user
    CREATE USER mu_dm WITH LOGIN PASSWORD '$MU_DATA_DB_PASSWORD_DM';
    GRANT CREATE ON DATABASE art_universe TO mu_dm;

    -- Quiz data user
    CREATE USER mu_quiz_dm WITH LOGIN PASSWORD '$MU_QUIZ_DB_PASSWORD_DM';
    GRANT CREATE ON DATABASE art_universe TO mu_quiz_dm;

    -- Art data user
    CREATE USER art_dm WITH LOGIN PASSWORD '$ART_DATA_DB_PASSWORD_DM';
    GRANT CREATE ON DATABASE art_universe TO art_dm;

    -- =============================================================================
    -- LASTFM RAW DATA SCHEMAS
    -- =============================================================================
    CREATE SCHEMA IF NOT EXISTS mu_raw_lastfm AUTHORIZATION mu_raw_lastfm_dm;
    GRANT ALL PRIVILEGES ON SCHEMA mu_raw_lastfm TO mu_raw_lastfm_dm;
    ALTER DEFAULT PRIVILEGES IN SCHEMA mu_raw_lastfm GRANT ALL ON TABLES TO mu_raw_lastfm_dm;
    ALTER DEFAULT PRIVILEGES IN SCHEMA mu_raw_lastfm GRANT ALL ON SEQUENCES TO mu_raw_lastfm_dm;
    ALTER DEFAULT PRIVILEGES IN SCHEMA mu_raw_lastfm GRANT ALL ON FUNCTIONS TO mu_raw_lastfm_dm;

    CREATE SCHEMA IF NOT EXISTS mu_raw_lastfm_staging AUTHORIZATION mu_raw_lastfm_dm;
    GRANT ALL PRIVILEGES ON SCHEMA mu_raw_lastfm_staging TO mu_raw_lastfm_dm;
    ALTER DEFAULT PRIVILEGES IN SCHEMA mu_raw_lastfm_staging GRANT ALL ON TABLES TO mu_raw_lastfm_dm;
    ALTER DEFAULT PRIVILEGES IN SCHEMA mu_raw_lastfm_staging GRANT ALL ON SEQUENCES TO mu_raw_lastfm_dm;
    ALTER DEFAULT PRIVILEGES IN SCHEMA mu_raw_lastfm_staging GRANT ALL ON FUNCTIONS TO mu_raw_lastfm_dm;

    -- LastFM reader permissions
    GRANT USAGE ON SCHEMA mu_raw_lastfm TO mu_raw_lastfm_reader;
    GRANT USAGE ON SCHEMA mu_raw_lastfm_staging TO mu_raw_lastfm_reader;
    GRANT SELECT ON ALL TABLES IN SCHEMA mu_raw_lastfm TO mu_raw_lastfm_reader;
    GRANT SELECT ON ALL TABLES IN SCHEMA mu_raw_lastfm_staging TO mu_raw_lastfm_reader;

    -- Grant SELECT on future tables created by mu_raw_lastfm_dm
    ALTER DEFAULT PRIVILEGES FOR ROLE mu_raw_lastfm_dm IN SCHEMA mu_raw_lastfm GRANT SELECT ON TABLES TO mu_raw_lastfm_reader;
    ALTER DEFAULT PRIVILEGES FOR ROLE mu_raw_lastfm_dm IN SCHEMA mu_raw_lastfm_staging GRANT SELECT ON TABLES TO mu_raw_lastfm_reader;

    -- Grant SELECT on future tables created by postgres (for Liquibase migrations)
    ALTER DEFAULT PRIVILEGES FOR ROLE postgres IN SCHEMA mu_raw_lastfm GRANT SELECT ON TABLES TO mu_raw_lastfm_reader;
    ALTER DEFAULT PRIVILEGES FOR ROLE postgres IN SCHEMA mu_raw_lastfm_staging GRANT SELECT ON TABLES TO mu_raw_lastfm_reader;

    -- Set search paths for lastfm users
    ALTER ROLE mu_raw_lastfm_dm SET search_path TO mu_raw_lastfm,mu_raw_lastfm_staging,public;
    ALTER ROLE mu_raw_lastfm_reader SET search_path TO mu_raw_lastfm,mu_raw_lastfm_staging,public;

    -- =============================================================================
    -- MASTER DATA SCHEMA
    -- =============================================================================
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

# Configure pg_hba.conf for replication
echo "host replication replicator 0.0.0.0/0 md5" >> "$PGDATA/pg_hba.conf"

# Create a physical replication slot to ensure WAL segments are retained until consumed by the replica
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "art_universe" <<-EOSQL
    SELECT pg_create_physical_replication_slot('replica_slot');
EOSQL
