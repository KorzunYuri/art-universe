--liquibase formatted sql

--changeset system:0015_0010_sync-data-source
--comment: Sync DataSource enum values to dictionary table

INSERT INTO mu.dictionary (domain, code, name)
VALUES
    ('DataSource', 1, 'lastfm'),
    ('DataSource', 2, 'spotify'),
    ('DataSource', 3, 'musicbrainz')
ON CONFLICT (domain, code) DO UPDATE SET
    name = EXCLUDED.name;

--rollback DELETE FROM mu.dictionary WHERE domain = 'DataSource';
