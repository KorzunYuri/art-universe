#!/bin/bash
# Configure pg_hba.conf for replication connections
# This script runs AFTER init.sql during initial database setup
# The numeric prefix ensures execution order

set -e

echo "Configuring pg_hba.conf for replication..."

# Append replication connection rules to pg_hba.conf
cat >> /var/lib/postgresql/data/pg_hba.conf << 'EOF'

# Replication connections for streaming replication
host    replication     replicator      172.16.0.0/12           md5
host    replication     replicator      192.168.0.0/16          md5
host    replication     replicator      10.0.0.0/8              md5
host    replication     replicator      127.0.0.1/32            md5
host    replication     replicator      ::1/128                 md5
EOF

echo "pg_hba.conf configured successfully for replication"
echo "Reloading PostgreSQL configuration..."

# Signal PostgreSQL to reload configuration
psql -v ON_ERROR_STOP=1 --username "postgres" <<-EOSQL
    SELECT pg_reload_conf();
EOSQL

echo "Configuration reloaded successfully"
