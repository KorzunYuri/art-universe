-- Seed COMPOSITE attribute definitions (computation_type=3)
-- These attributes are calculated from data across multiple sources.
--
-- Data types: NUMERIC(1), STRING(2), DATE(3), BOOLEAN(4)
-- Temporal types: CONSTANT(1), INSTANT(2), PERIOD(3)
-- Target types: ARTIST(1), ALBUM(2), TRACK(3)

-- === COMPOSITE (201-202) ===
-- IDs start at 201 to leave room for future DERIVED additions
INSERT INTO attribute_def (id, code, name, description, data_type, temporal_type, computation_type, data_source, formula, is_system) VALUES
    (201, 'combined_popularity', 'Combined Popularity Score',
        'Normalized popularity score combining Spotify popularity (0-100) with LastFM listeners and play counts',
        1, 3, 3, 'multiple',
        'normalize(spotify.popularity, lastfm.listeners_count, lastfm.play_count)',
        true),
    (202, 'combined_duration_ms', 'Duration (ms, reconciled)',
        'Track duration in ms reconciled across sources; prefers Spotify, falls back to LastFM',
        1, 1, 3, 'multiple',
        'COALESCE(spotify.duration_ms, lastfm.duration * 1000)',
        true);

-- === Applicability ===
INSERT INTO attribute_def_applicability (id, attribute_def_id, target_type) VALUES
    -- combined_popularity → ARTIST, ALBUM, TRACK
    (201, 201, 1),
    (202, 201, 2),
    (203, 201, 3),
    -- combined_duration_ms → TRACK
    (204, 202, 3);
