--liquibase formatted sql

--changeset system:0038_0020_sync-coordination-status
--comment: Sync CoordinationStatus enum values to dictionary table

INSERT INTO mu_raw_lastfm.dictionary (domain, code, name)
VALUES
    ('CoordinationStatus', 1, 'NORMAL'),
    ('CoordinationStatus', 2, 'REQUESTED'),
    ('CoordinationStatus', 3, 'RUNNING')
ON CONFLICT (domain, code) DO UPDATE SET
    name = EXCLUDED.name;
