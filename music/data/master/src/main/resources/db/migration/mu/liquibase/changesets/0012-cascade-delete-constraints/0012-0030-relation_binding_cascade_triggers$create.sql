-- Triggers for artist_binding
CREATE TRIGGER trg_cascade_delete_artist_category_bindings
    BEFORE DELETE ON artist_binding
    FOR EACH ROW
    EXECUTE FUNCTION fn_cascade_delete_artist_category_bindings();

CREATE TRIGGER trg_cascade_delete_artist_track_bindings
    BEFORE DELETE ON artist_binding
    FOR EACH ROW
    EXECUTE FUNCTION fn_cascade_delete_artist_track_bindings();

-- Trigger for category_binding
CREATE TRIGGER trg_cascade_delete_category_artist_bindings
    BEFORE DELETE ON category_binding
    FOR EACH ROW
    EXECUTE FUNCTION fn_cascade_delete_category_artist_bindings();

-- Trigger for track_binding
CREATE TRIGGER trg_cascade_delete_track_artist_bindings
    BEFORE DELETE ON track_binding
    FOR EACH ROW
    EXECUTE FUNCTION fn_cascade_delete_track_artist_bindings();
