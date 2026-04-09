-- Add readonly views for person relation tables
CREATE OR REPLACE VIEW mu_view.v_artist_person AS
SELECT id, artist_id, person_id, relation_type_id FROM mu.artist_person;

CREATE OR REPLACE VIEW mu_view.v_album_person AS
SELECT id, album_id, person_id, relation_type_id FROM mu.album_person;

CREATE OR REPLACE VIEW mu_view.v_track_person AS
SELECT id, track_id, person_id, relation_type_id FROM mu.track_person;

-- Grant permissions
GRANT SELECT ON mu_view.v_artist_person TO PUBLIC;
GRANT SELECT ON mu_view.v_album_person TO PUBLIC;
GRANT SELECT ON mu_view.v_track_person TO PUBLIC;
