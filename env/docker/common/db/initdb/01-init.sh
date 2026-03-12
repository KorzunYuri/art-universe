#!/bin/bash
# Art Universe - Incremental database initialization
#
# Applies SQL changesets from ./changesets/ in alphabetical order.
# Each changeset is applied exactly once; applied filenames are tracked
# in the 'init_changesets' table of the postgres system database.
#
# To add new initialization (users, schemas, grants, etc.):
#   1. Create changesets/NNNN-description.sql
#   2. Re-run this script (or run psql directly for the new file only)
#
# Changeset file conventions:
#   - 0001-*.sql  runs against the postgres system database (for CREATE DATABASE)
#   - All other *.sql files run against art_universe
#   - Use ${ENV_VAR} for passwords; they are substituted via envsubst before execution
#

set -e

# Test environment defaults — bash assigns these only when the variable is unset or empty.
# In production all variables are set by docker-compose env files, so these are no-ops.
: "${MURAW_LASTFM_DB_REPLICATION_PASSWORD:=test_password}"
: "${MURAW_LASTFM_DB_WRITER_PASSWORD:=test_password}"
: "${MURAW_LASTFM_DB_READER_PASSWORD:=test_password}"
: "${MU_DATA_DB_PASSWORD_DM:=test_password}"
: "${MU_QUIZ_DB_PASSWORD_DM:=test_password}"
: "${ART_DATA_DB_PASSWORD_DM:=test_password}"
: "${MURAW_SPOTIFY_DB_WRITER_PASSWORD:=test_password}"
: "${MURAW_SPOTIFY_DB_READER_PASSWORD:=test_password}"
: "${MURAW_SPOTIFY_CALLS_GENERATOR_DB_PASSWORD:=test_password}"
: "${MURAW_SPOTIFY_CALLS_PERFORMER_DB_PASSWORD:=test_password}"
: "${MURAW_SPOTIFY_RESPONSE_PARSER_DB_PASSWORD:=test_password}"
: "${MURAW_SPOTIFY_STAGING_APPLICATOR_DB_PASSWORD:=test_password}"
: "${AU_CONFIG_DB_PASSWORD_DM:=test_password}"

CHANGESETS_DIR="$(dirname "${BASH_SOURCE[0]:-$0}")/changesets"

# Portable envsubst replacement — works on alpine without gettext.
# Only substitutes the password variables used in changeset SQL files.
substitute_env_vars() {
    sed \
        -e "s|\${MURAW_LASTFM_DB_REPLICATION_PASSWORD}|${MURAW_LASTFM_DB_REPLICATION_PASSWORD}|g" \
        -e "s|\${MURAW_LASTFM_DB_WRITER_PASSWORD}|${MURAW_LASTFM_DB_WRITER_PASSWORD}|g" \
        -e "s|\${MURAW_LASTFM_DB_READER_PASSWORD}|${MURAW_LASTFM_DB_READER_PASSWORD}|g" \
        -e "s|\${MU_DATA_DB_PASSWORD_DM}|${MU_DATA_DB_PASSWORD_DM}|g" \
        -e "s|\${MU_QUIZ_DB_PASSWORD_DM}|${MU_QUIZ_DB_PASSWORD_DM}|g" \
        -e "s|\${ART_DATA_DB_PASSWORD_DM}|${ART_DATA_DB_PASSWORD_DM}|g" \
        -e "s|\${MURAW_SPOTIFY_DB_WRITER_PASSWORD}|${MURAW_SPOTIFY_DB_WRITER_PASSWORD}|g" \
        -e "s|\${MURAW_SPOTIFY_DB_READER_PASSWORD}|${MURAW_SPOTIFY_DB_READER_PASSWORD}|g" \
        -e "s|\${MURAW_SPOTIFY_CALLS_GENERATOR_DB_PASSWORD}|${MURAW_SPOTIFY_CALLS_GENERATOR_DB_PASSWORD}|g" \
        -e "s|\${MURAW_SPOTIFY_CALLS_PERFORMER_DB_PASSWORD}|${MURAW_SPOTIFY_CALLS_PERFORMER_DB_PASSWORD}|g" \
        -e "s|\${MURAW_SPOTIFY_RESPONSE_PARSER_DB_PASSWORD}|${MURAW_SPOTIFY_RESPONSE_PARSER_DB_PASSWORD}|g" \
        -e "s|\${MURAW_SPOTIFY_STAGING_APPLICATOR_DB_PASSWORD}|${MURAW_SPOTIFY_STAGING_APPLICATOR_DB_PASSWORD}|g" \
        -e "s|\${AU_CONFIG_DB_PASSWORD_DM}|${AU_CONFIG_DB_PASSWORD_DM}|g" \
        "$1"
}

# ============================================================
# Bootstrap: tracking table in the postgres system database
# ============================================================
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    CREATE TABLE IF NOT EXISTS init_changesets (
        filename   TEXT        PRIMARY KEY,
        applied_at TIMESTAMPTZ NOT NULL DEFAULT now()
    );
EOSQL

# ============================================================
# Apply each changeset exactly once, in alphabetical order
# ============================================================
apply_changeset() {
    local file="$1"
    local filename
    filename=$(basename "$file")

    # 0001-* targets the postgres system database; everything else targets art_universe
    local db
    if [[ "$filename" == 0001-* ]]; then
        db="$POSTGRES_DB"
    else
        db="art_universe"
    fi

    local already_applied
    already_applied=$(
        psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" \
             -t -c "SELECT count(*) FROM init_changesets WHERE filename = '$filename'" \
        | tr -d '[:space:]'
    )

    if [ "$already_applied" -eq "0" ]; then
        echo "[init] Applying:  $filename"
        substitute_env_vars "$file" \
            | psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$db"
        psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" \
             -c "INSERT INTO init_changesets (filename) VALUES ('$filename')"
        echo "[init] Applied:   $filename"
    else
        echo "[init] Skipping (already applied): $filename"
    fi
}

for changeset_file in $(ls "$CHANGESETS_DIR"/*.sql 2>/dev/null | sort); do
    apply_changeset "$changeset_file"
done

# ============================================================
# Bash-only operations (re-evaluated on every container start)
# ============================================================

# pg_hba.conf: allow streaming replication from the replica container
if ! grep -q "host replication replicator" "$PGDATA/pg_hba.conf"; then
    echo "host replication replicator 0.0.0.0/0 md5" >> "$PGDATA/pg_hba.conf"
    echo "[init] Configured pg_hba.conf for replication"
fi
