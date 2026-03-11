-- Create the art_universe database (idempotent)
SELECT 'CREATE DATABASE art_universe'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'art_universe')
\gexec
