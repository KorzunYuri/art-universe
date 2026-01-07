-- Create readonly views with minimal fields for consumer modules

-- Artist view - only id and name
CREATE OR REPLACE VIEW mu_view.v_artist AS
SELECT id, name FROM mu.artist;

-- Album view - id, name, primary_artist_id, album_group_id
CREATE OR REPLACE VIEW mu_view.v_album AS
SELECT id, name, primary_artist_id, album_group_id FROM mu.album;

-- Track view - id, name, primary_artist_id, track_group_id
CREATE OR REPLACE VIEW mu_view.v_track AS
SELECT id, name, primary_artist_id, track_group_id FROM mu.track;

-- Category view - id, name, parent_id, dimension_id
CREATE OR REPLACE VIEW mu_view.v_category AS
SELECT id, name, parent_id, dimension_id FROM mu.category;

-- Dimension view - id, name
CREATE OR REPLACE VIEW mu_view.v_dimension AS
SELECT id, name FROM mu.dimension;

-- Artist-Category relation view - id, artist_id, category_id
CREATE OR REPLACE VIEW mu_view.v_artist_category AS
SELECT id, artist_id, category_id FROM mu.artist_category;

-- Artist-Track relation view - id, artist_id, track_id
CREATE OR REPLACE VIEW mu_view.v_artist_track AS
SELECT id, artist_id, track_id FROM mu.artist_track;

-- Category hierarchy view - using actual materialized view structure
CREATE OR REPLACE VIEW mu_view.v_category_hierarchy AS
SELECT id, name, dimension_id, effective_dimension_id, parent_id, hierarchy_level 
FROM mu.mv_category_hierarchy;

-- Dictionary view - code, name, domain
CREATE OR REPLACE VIEW mu_view.v_dictionary AS
SELECT code, name, domain FROM mu.dictionary;
