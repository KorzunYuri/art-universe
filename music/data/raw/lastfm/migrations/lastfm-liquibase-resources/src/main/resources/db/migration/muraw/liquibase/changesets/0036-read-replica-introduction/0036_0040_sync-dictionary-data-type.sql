--liquibase formatted sql

--changeset system:0038_0030_sync-data-type
--comment: Sync LastfmAttribute.DataType enum values to dictionary table

INSERT INTO mu_raw_lastfm.dictionary (domain, code, name)
VALUES
    ('DataType', 1, 'STRING'),
    ('DataType', 2, 'NUMERIC'),
    ('DataType', 3, 'BOOLEAN')
ON CONFLICT (domain, code) DO UPDATE SET
    name = EXCLUDED.name;
