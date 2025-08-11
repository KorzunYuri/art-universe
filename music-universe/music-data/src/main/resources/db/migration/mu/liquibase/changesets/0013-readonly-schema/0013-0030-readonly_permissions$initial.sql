-- Grant SELECT permissions on views to all users
GRANT SELECT ON mu_view.v_artist TO PUBLIC;
GRANT SELECT ON mu_view.v_album TO PUBLIC;
GRANT SELECT ON mu_view.v_track TO PUBLIC;
GRANT SELECT ON mu_view.v_category TO PUBLIC;
GRANT SELECT ON mu_view.v_dimension TO PUBLIC;
GRANT SELECT ON mu_view.v_artist_category TO PUBLIC;
GRANT SELECT ON mu_view.v_artist_track TO PUBLIC;
GRANT SELECT ON mu_view.v_category_hierarchy TO PUBLIC;
GRANT SELECT ON mu_view.v_dictionary TO PUBLIC;
