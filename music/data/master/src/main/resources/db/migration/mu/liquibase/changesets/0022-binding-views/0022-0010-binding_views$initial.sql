CREATE OR REPLACE VIEW mu_view.v_artist_binding AS
SELECT id, master_id, data_source_id, external_id, origin, approval_status, created_at, updated_at
FROM mu.artist_binding;

CREATE OR REPLACE VIEW mu_view.v_album_binding AS
SELECT id, master_id, data_source_id, external_id, origin, approval_status, created_at, updated_at
FROM mu.album_binding;

CREATE OR REPLACE VIEW mu_view.v_track_binding AS
SELECT id, master_id, data_source_id, external_id, origin, approval_status, created_at, updated_at
FROM mu.track_binding;

GRANT SELECT ON mu_view.v_artist_binding TO PUBLIC;
GRANT SELECT ON mu_view.v_album_binding TO PUBLIC;
GRANT SELECT ON mu_view.v_track_binding TO PUBLIC;
