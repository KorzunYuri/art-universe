#!/bin/bash
set -e

echo "Waiting for master to be ready..."
RETRY_COUNT=0
MAX_RETRIES=30

until PGPASSWORD=$POSTGRES_REPLICATION_PASSWORD psql -h "$POSTGRES_MASTER_HOST" -U "$POSTGRES_REPLICATION_USER" -p "$POSTGRES_MASTER_PORT" -d postgres -c '\q' 2>&1; do
  RETRY_COUNT=$((RETRY_COUNT + 1))
  echo "Master not ready (attempt $RETRY_COUNT/$MAX_RETRIES). Error shown above."

  if [ $RETRY_COUNT -ge $MAX_RETRIES ]; then
    echo "ERROR: Failed to connect to master after $MAX_RETRIES attempts"
    echo "Last connection attempt details:"
    echo "  Host: $POSTGRES_MASTER_HOST"
    echo "  Port: $POSTGRES_MASTER_PORT"
    echo "  User: $POSTGRES_REPLICATION_USER"
    echo "  Database: postgres"
    exit 1
  fi

  sleep 2
done

echo "Master is ready. Setting up streaming replication..."

# Clear data directory
rm -rf /var/lib/postgresql/data/*

# Perform base backup from master
PGPASSWORD=$POSTGRES_REPLICATION_PASSWORD pg_basebackup \
  -h "$POSTGRES_MASTER_HOST" \
  -p "$POSTGRES_MASTER_PORT" \
  -U "$POSTGRES_REPLICATION_USER" \
  -D /var/lib/postgresql/data \
  -Fp \
  -Xs \
  -P \
  -R

echo "Replica setup complete. Starting PostgreSQL in replica mode..."
