-- Drop relation binding tables and their sequences.
-- These tables mapped external relation pairs to internal relation rows (layer 3).
-- This layer is now redundant — internal relations (layer 2) serve as the approval signal directly.

DROP TABLE IF EXISTS artist_category_binding CASCADE;
DROP SEQUENCE IF EXISTS artist_category_binding_seq;

DROP TABLE IF EXISTS artist_album_binding CASCADE;
DROP SEQUENCE IF EXISTS artist_album_binding_seq;

DROP TABLE IF EXISTS artist_track_binding CASCADE;
DROP SEQUENCE IF EXISTS artist_track_binding_seq;

DROP TABLE IF EXISTS album_category_binding CASCADE;
DROP SEQUENCE IF EXISTS album_category_binding_seq;

DROP TABLE IF EXISTS album_track_binding CASCADE;
DROP SEQUENCE IF EXISTS album_track_binding_seq;

DROP TABLE IF EXISTS track_category_binding CASCADE;
DROP SEQUENCE IF EXISTS track_category_binding_seq;
