-- Replication user and replication slot (idempotent)

DO $replicator$
BEGIN
  IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'replicator') THEN
    CREATE USER replicator WITH REPLICATION LOGIN PASSWORD '${MURAW_LASTFM_DB_REPLICATION_PASSWORD}';
  END IF;
END;
$replicator$;

SELECT pg_create_physical_replication_slot('replica_slot')
WHERE NOT EXISTS (SELECT 1 FROM pg_replication_slots WHERE slot_name = 'replica_slot');
