#!/bin/bash
# Replica entrypoint script for PostgreSQL streaming replication
# Replaces the default docker-entrypoint.sh to bootstrap from master via pg_basebackup
#
# Required environment variables:
#   POSTGRES_MASTER_HOST              - master hostname
#   POSTGRES_MASTER_PORT              - master port
#   POSTGRES_REPLICATION_USER         - replication user
#   MURAW_LASTFM_DB_REPLICATION_PASSWORD - replication password
#   PGDATA                            - postgres data directory (set by image, default: /var/lib/postgresql/data)

set -e

# If the data directory has no PG_VERSION, it's uninitialized — bootstrap from master
if [ ! -s "$PGDATA/PG_VERSION" ]; then
    echo "Replica: data directory is empty, running pg_basebackup from master..."

    # Clean any partial data
    rm -rf "${PGDATA:?}"/*

    # Run pg_basebackup:
    #   -R : create standby.signal and write primary_conninfo to postgresql.auto.conf
    #   -X stream : stream WAL during backup for consistency
    #   -S replica_slot : use existing replication slot (created by master init)
    PGPASSWORD="$MURAW_LASTFM_DB_REPLICATION_PASSWORD" pg_basebackup \
        -h "$POSTGRES_MASTER_HOST" \
        -p "$POSTGRES_MASTER_PORT" \
        -U "$POSTGRES_REPLICATION_USER" \
        -D "$PGDATA" \
        -Fp -Xs -P -R \
        -S replica_slot

    chmod 0700 "$PGDATA"
    echo "Replica: pg_basebackup complete, starting PostgreSQL in standby mode."
fi

# Hand off to the standard postgres entrypoint
exec docker-entrypoint.sh postgres
