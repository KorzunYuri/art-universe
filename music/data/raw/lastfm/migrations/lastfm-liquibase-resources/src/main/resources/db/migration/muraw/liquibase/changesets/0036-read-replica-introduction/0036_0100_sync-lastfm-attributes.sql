--liquibase formatted sql

--changeset system:0037_0010_sync-lastfm-attributes
--comment: Sync LastfmAttribute enum values to attribute dictionary

-- Insert or update attributes based on LastfmAttribute enum
-- This migration is idempotent and safe to run multiple times

INSERT INTO attribute (id, name, description, type, updated_at)
VALUES
    (1, 'Relations number', 'Number of entities associated with entity', 2, NOW()),
    (2, 'Usage count', 'Number of users that used entity', 2, NOW()),
    (3, 'URL', 'Entity''s URL', 1, NOW()),
    (4, 'rank', 'Rank of the entity, global or scoped', 2, NOW()),
    (5, 'mbid', 'MusicBrainz ID', 1, NOW()),
    (6, 'duration', 'Track duration', 2, NOW()),
    (7, 'streamable', 'Is streamable', 3, NOW()),
    (8, 'on_tour', 'Artist is on tour', 3, NOW()),
    (9, 'listeners_count', 'Number of listeners', 2, NOW()),
    (10, 'play_count', 'How many times track(s) were scrobbled', 2, NOW()),
    (11, 'match_coeff', 'Represents how much two entities are similar to each other, %', 2, NOW())
ON CONFLICT (id) DO NOTHING;
