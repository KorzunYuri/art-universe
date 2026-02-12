-- Update existing artist_track view to include relation_type_id
CREATE OR REPLACE VIEW mu_view.v_artist_track AS
SELECT id, artist_id, track_id, relation_type_id FROM mu.artist_track;

-- New cross-entity junction views
CREATE OR REPLACE VIEW mu_view.v_artist_album AS
SELECT id, artist_id, album_id, relation_type_id FROM mu.artist_album;

CREATE OR REPLACE VIEW mu_view.v_album_track AS
SELECT id, album_id, track_id, relation_type_id FROM mu.album_track;

-- New same-entity junction views
CREATE OR REPLACE VIEW mu_view.v_artist_artist AS
SELECT id, source_artist_id, target_artist_id, relation_type_id FROM mu.artist_artist;

CREATE OR REPLACE VIEW mu_view.v_album_album AS
SELECT id, source_album_id, target_album_id, relation_type_id FROM mu.album_album;

CREATE OR REPLACE VIEW mu_view.v_track_track AS
SELECT id, source_track_id, target_track_id, relation_type_id FROM mu.track_track;

-- Relation type dictionary views
CREATE OR REPLACE VIEW mu_view.v_relation_type AS
SELECT id, name, reverse_name, is_symmetrical FROM mu.relation_type;

CREATE OR REPLACE VIEW mu_view.v_relation_type_applicability AS
SELECT id, relation_type_id, source_entity_type, target_entity_type FROM mu.relation_type_applicability;

-- Grant permissions
GRANT SELECT ON mu_view.v_artist_album TO PUBLIC;
GRANT SELECT ON mu_view.v_album_track TO PUBLIC;
GRANT SELECT ON mu_view.v_artist_artist TO PUBLIC;
GRANT SELECT ON mu_view.v_album_album TO PUBLIC;
GRANT SELECT ON mu_view.v_track_track TO PUBLIC;
GRANT SELECT ON mu_view.v_relation_type TO PUBLIC;
GRANT SELECT ON mu_view.v_relation_type_applicability TO PUBLIC;
