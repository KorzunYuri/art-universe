-- Seed DERIVED attribute definitions (computation_type=2)
-- These attributes are calculated from other attributes within a single data source.
--
-- Data types: NUMERIC(1), STRING(2), DATE(3), BOOLEAN(4)
-- Temporal types: CONSTANT(1), INSTANT(2), PERIOD(3)
-- Target types: ARTIST(1), ALBUM(2), TRACK(3)

-- === DERIVED from LastFM (101-103) ===
-- IDs start at 101 to leave room for future RAW additions
INSERT INTO attribute_def (id, code, name, description, data_type, temporal_type, computation_type, data_source, formula, is_system) VALUES
    (101, 'rank_in_artist_by_listeners',  'Rank in Artist (by Listeners)',
        'Track rank within its artist ordered by listener count descending',
        1, 3, 2, 'lastfm',
        'RANK() OVER (PARTITION BY artist_id ORDER BY listeners DESC)',
        true),
    (102, 'rank_in_artist_by_playcount',  'Rank in Artist (by Play Count)',
        'Track rank within its artist ordered by play count descending',
        1, 3, 2, 'lastfm',
        'RANK() OVER (PARTITION BY artist_id ORDER BY playcount DESC)',
        true),
    (103, 'rank_in_album_by_playcount',   'Rank in Album (by Play Count)',
        'Track rank within its album ordered by play count descending',
        1, 3, 2, 'lastfm',
        'RANK() OVER (PARTITION BY album_id ORDER BY playcount DESC)',
        true);

-- === Applicability ===
INSERT INTO attribute_def_applicability (id, attribute_def_id, target_type) VALUES
    -- rank_in_artist_by_listeners → TRACK
    (101, 101, 3),
    -- rank_in_artist_by_playcount → TRACK
    (102, 102, 3),
    -- rank_in_album_by_playcount → TRACK
    (103, 103, 3);
