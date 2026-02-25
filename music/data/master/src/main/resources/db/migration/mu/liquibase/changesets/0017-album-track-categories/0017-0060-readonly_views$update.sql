-- Album category views
CREATE OR REPLACE VIEW mu_view.v_album_category AS
SELECT id, album_id, category_id FROM mu.album_category;

CREATE OR REPLACE VIEW mu_view.v_album_category_binding AS
SELECT id, master_id, data_source_id, external_album_id, external_category_id FROM mu.album_category_binding;

-- Track category views
CREATE OR REPLACE VIEW mu_view.v_track_category AS
SELECT id, track_id, category_id FROM mu.track_category;

CREATE OR REPLACE VIEW mu_view.v_track_category_binding AS
SELECT id, master_id, data_source_id, external_track_id, external_category_id FROM mu.track_category_binding;
