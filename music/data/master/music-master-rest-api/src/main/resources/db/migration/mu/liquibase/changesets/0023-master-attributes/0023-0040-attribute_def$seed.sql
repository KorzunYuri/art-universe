-- Seed RAW attribute definitions (computation_type=1)
-- These attributes are passed through directly from a single data source.
--
-- Data types: NUMERIC(1), STRING(2), DATE(3), BOOLEAN(4)
-- Temporal types: CONSTANT(1), INSTANT(2), PERIOD(3)
-- Target types: ARTIST(1), ALBUM(2), TRACK(3), CATEGORY(4),
--               ARTIST_ARTIST(10), ALBUM_TRACK(14)

-- === RAW from LastFM (1-11) ===
INSERT INTO attribute_def (id, code, name, data_type, temporal_type, computation_type, data_source, is_system) VALUES
    (1,  'mbid',                'MusicBrainz ID',           2, 1, 1, 'lastfm', true),
    (2,  'listeners_count',     'Listeners Count',          1, 3, 1, 'lastfm', true),
    (3,  'play_count',          'Play Count',               1, 3, 1, 'lastfm', true),
    (4,  'on_tour',             'On Tour',                  4, 2, 1, 'lastfm', true),
    (5,  'bio_summary',         'Biography Summary',        2, 1, 1, 'lastfm', true),
    (6,  'bio_published_date',  'Bio Published Date',       3, 1, 1, 'lastfm', true),
    (7,  'wiki_summary',        'Wiki Summary',             2, 1, 1, 'lastfm', true),
    (8,  'wiki_published_date', 'Wiki Published Date',      3, 1, 1, 'lastfm', true),
    (9,  'streamable',          'Streamable',               4, 1, 1, 'lastfm', true),
    (10, 'total_tagged',        'Total Tagged Items',       1, 3, 1, 'lastfm', true),
    (11, 'reach',               'Reach (Unique Listeners)', 1, 3, 1, 'lastfm', true);

-- === RAW from Spotify (12-22) ===
INSERT INTO attribute_def (id, code, name, data_type, temporal_type, computation_type, data_source, is_system) VALUES
    (12, 'popularity',          'Popularity',               1, 3, 1, 'spotify', true),
    (13, 'followers_count',     'Followers Count',          1, 3, 1, 'spotify', true),
    (14, 'release_date',        'Release Date',             3, 2, 1, 'spotify', true),
    (15, 'album_type',          'Album Type',               2, 1, 1, 'spotify', true),
    (16, 'total_tracks',        'Total Tracks',             1, 1, 1, 'spotify', true),
    (17, 'label',               'Record Label',             2, 1, 1, 'spotify', true),
    (18, 'duration_ms',         'Duration (ms)',            1, 1, 1, 'spotify', true),
    (19, 'explicit',            'Explicit',                 4, 1, 1, 'spotify', true),
    (20, 'isrc',                'ISRC',                     2, 1, 1, 'spotify', true),
    (21, 'disc_number',         'Disc Number',              1, 1, 1, 'spotify', true),
    (22, 'track_number',        'Track Number',             1, 1, 1, 'spotify', true);

-- === RAW relation attributes (23-24) ===
INSERT INTO attribute_def (id, code, name, data_type, temporal_type, computation_type, data_source, is_system) VALUES
    (23, 'match_coeff',         'Match Coefficient',        1, 2, 1, 'lastfm', true),
    (24, 'track_order',         'Track Order',              1, 1, 1, 'lastfm', true);

-- === Applicability: LastFM entity attributes ===
INSERT INTO attribute_def_applicability (id, attribute_def_id, target_type) VALUES
    -- mbid → ARTIST, ALBUM, TRACK
    (1,  1,  1),
    (2,  1,  2),
    (3,  1,  3),
    -- listeners_count → ARTIST, ALBUM, TRACK
    (4,  2,  1),
    (5,  2,  2),
    (6,  2,  3),
    -- play_count → ARTIST, ALBUM, TRACK
    (7,  3,  1),
    (8,  3,  2),
    (9,  3,  3),
    -- on_tour → ARTIST
    (10, 4,  1),
    -- bio_summary → ARTIST
    (11, 5,  1),
    -- bio_published_date → ARTIST
    (12, 6,  1),
    -- wiki_summary → ALBUM, TRACK
    (13, 7,  2),
    (14, 7,  3),
    -- wiki_published_date → ALBUM, TRACK
    (15, 8,  2),
    (16, 8,  3),
    -- streamable → TRACK
    (17, 9,  3),
    -- total_tagged → CATEGORY
    (18, 10, 4),
    -- reach → CATEGORY
    (19, 11, 4);

-- === Applicability: Spotify entity attributes ===
INSERT INTO attribute_def_applicability (id, attribute_def_id, target_type) VALUES
    -- popularity → ARTIST, ALBUM, TRACK
    (20, 12, 1),
    (21, 12, 2),
    (22, 12, 3),
    -- followers_count → ARTIST
    (23, 13, 1),
    -- release_date → ALBUM
    (24, 14, 2),
    -- album_type → ALBUM
    (25, 15, 2),
    -- total_tracks → ALBUM
    (26, 16, 2),
    -- label → ALBUM
    (27, 17, 2),
    -- duration_ms → TRACK
    (28, 18, 3),
    -- explicit → TRACK
    (29, 19, 3),
    -- isrc → TRACK
    (30, 20, 3),
    -- disc_number → TRACK
    (31, 21, 3),
    -- track_number → TRACK
    (32, 22, 3);

-- === Applicability: Relation attributes ===
INSERT INTO attribute_def_applicability (id, attribute_def_id, target_type) VALUES
    -- match_coeff → ARTIST_ARTIST
    (33, 23, 10),
    -- track_order → ALBUM_TRACK
    (34, 24, 14);
