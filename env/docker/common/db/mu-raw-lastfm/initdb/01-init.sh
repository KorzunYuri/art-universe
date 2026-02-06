#!/bin/bash
# LastFM master database initialization script
# Shared by Docker Compose and Kubernetes deployments
#
# Required environment variables:
#   POSTGRES_USER, POSTGRES_DB           - set by postgres image
#   MURAW_LASTFM_DB_WRITER_PASSWORD      - dm user password
#   MURAW_LASTFM_DB_READER_PASSWORD      - reader user password
#   MURAW_LASTFM_DB_REPLICATION_PASSWORD - replication user password
#   PGDATA                               - postgres data directory (set by image)

set -e

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    -- Create database
    CREATE DATABASE music_universe;
EOSQL

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "music_universe" <<-EOSQL
    -- Create data management user (write access)
    CREATE USER mu_raw_lastfm_dm WITH LOGIN PASSWORD '$MURAW_LASTFM_DB_WRITER_PASSWORD';

    -- Create read-only user for replica reads
    CREATE USER mu_raw_lastfm_reader WITH LOGIN PASSWORD '$MURAW_LASTFM_DB_READER_PASSWORD';

    -- Create replication user for streaming replication
    CREATE USER replicator WITH REPLICATION LOGIN PASSWORD '$MURAW_LASTFM_DB_REPLICATION_PASSWORD';

    -- Create schemas and grant write permissions to data management user
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

    -- Grant read-only permissions to reader user
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

    -- Set search paths
    ALTER ROLE mu_raw_lastfm_dm SET search_path TO mu_raw_lastfm,mu_raw_lastfm_staging,public;
    ALTER ROLE mu_raw_lastfm_reader SET search_path TO mu_raw_lastfm,mu_raw_lastfm_staging,public;
EOSQL

# Configure pg_hba.conf for replication
echo "host replication replicator 0.0.0.0/0 md5" >> "$PGDATA/pg_hba.conf"
