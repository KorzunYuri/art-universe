--liquibase formatted sql

--changeset system:0038_0010_sync-lastfm-entity-relation-type
--comment: Sync LastfmEntityRelationType enum values to dictionary table

INSERT INTO dictionary (domain, code, name)
VALUES
    ('LastfmEntityRelationType', 1, 'SIMILARITY')
ON CONFLICT (domain, code) DO UPDATE SET
    name = EXCLUDED.name;
