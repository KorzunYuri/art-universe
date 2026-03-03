#!/bin/bash
# Spotify schema initialization script
# Runs after 01-init.sh on the same 'music_universe' database
#
# Required environment variables:
#   POSTGRES_USER                        - set by postgres image
#   MURAW_SPOTIFY_DB_WRITER_PASSWORD     - dm user password
#   MURAW_SPOTIFY_DB_READER_PASSWORD     - reader user password

set -e

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "music_universe" <<-EOSQL
    -- Create data management user (write access)
    CREATE USER mu_raw_spotify_dm WITH LOGIN PASSWORD '$MURAW_SPOTIFY_DB_WRITER_PASSWORD';

    -- Create read-only user for replica reads
    CREATE USER mu_raw_spotify_reader WITH LOGIN PASSWORD '$MURAW_SPOTIFY_DB_READER_PASSWORD';

    -- Create schemas and grant write permissions to data management user
    CREATE SCHEMA IF NOT EXISTS mu_raw_spotify AUTHORIZATION mu_raw_spotify_dm;
    GRANT ALL PRIVILEGES ON SCHEMA mu_raw_spotify TO mu_raw_spotify_dm;
    ALTER DEFAULT PRIVILEGES IN SCHEMA mu_raw_spotify GRANT ALL ON TABLES TO mu_raw_spotify_dm;
    ALTER DEFAULT PRIVILEGES IN SCHEMA mu_raw_spotify GRANT ALL ON SEQUENCES TO mu_raw_spotify_dm;
    ALTER DEFAULT PRIVILEGES IN SCHEMA mu_raw_spotify GRANT ALL ON FUNCTIONS TO mu_raw_spotify_dm;

    CREATE SCHEMA IF NOT EXISTS mu_raw_spotify_staging AUTHORIZATION mu_raw_spotify_dm;
    GRANT ALL PRIVILEGES ON SCHEMA mu_raw_spotify_staging TO mu_raw_spotify_dm;
    ALTER DEFAULT PRIVILEGES IN SCHEMA mu_raw_spotify_staging GRANT ALL ON TABLES TO mu_raw_spotify_dm;
    ALTER DEFAULT PRIVILEGES IN SCHEMA mu_raw_spotify_staging GRANT ALL ON SEQUENCES TO mu_raw_spotify_dm;
    ALTER DEFAULT PRIVILEGES IN SCHEMA mu_raw_spotify_staging GRANT ALL ON FUNCTIONS TO mu_raw_spotify_dm;

    -- Grant read-only permissions to reader user
    GRANT USAGE ON SCHEMA mu_raw_spotify TO mu_raw_spotify_reader;
    GRANT USAGE ON SCHEMA mu_raw_spotify_staging TO mu_raw_spotify_reader;
    GRANT SELECT ON ALL TABLES IN SCHEMA mu_raw_spotify TO mu_raw_spotify_reader;
    GRANT SELECT ON ALL TABLES IN SCHEMA mu_raw_spotify_staging TO mu_raw_spotify_reader;

    -- Grant SELECT on future tables created by mu_raw_spotify_dm
    ALTER DEFAULT PRIVILEGES FOR ROLE mu_raw_spotify_dm IN SCHEMA mu_raw_spotify GRANT SELECT ON TABLES TO mu_raw_spotify_reader;
    ALTER DEFAULT PRIVILEGES FOR ROLE mu_raw_spotify_dm IN SCHEMA mu_raw_spotify_staging GRANT SELECT ON TABLES TO mu_raw_spotify_reader;

    -- Grant SELECT on future tables created by postgres (for Liquibase migrations)
    ALTER DEFAULT PRIVILEGES FOR ROLE postgres IN SCHEMA mu_raw_spotify GRANT SELECT ON TABLES TO mu_raw_spotify_reader;
    ALTER DEFAULT PRIVILEGES FOR ROLE postgres IN SCHEMA mu_raw_spotify_staging GRANT SELECT ON TABLES TO mu_raw_spotify_reader;

    -- Set search paths
    ALTER ROLE mu_raw_spotify_dm SET search_path TO mu_raw_spotify,mu_raw_spotify_staging,public;
    ALTER ROLE mu_raw_spotify_reader SET search_path TO mu_raw_spotify,mu_raw_spotify_staging,public;
EOSQL
