-- Function to delete album_category_binding when album_binding is deleted
CREATE OR REPLACE FUNCTION fn_cascade_delete_album_category_bindings()
RETURNS TRIGGER AS $$
BEGIN
    DELETE FROM album_category_binding
    WHERE data_source_id = OLD.data_source_id
      AND external_album_id = OLD.external_id;
    RETURN OLD;
END;
$$ LANGUAGE plpgsql;

-- Function to delete track_category_binding when track_binding is deleted
CREATE OR REPLACE FUNCTION fn_cascade_delete_track_category_bindings()
RETURNS TRIGGER AS $$
BEGIN
    DELETE FROM track_category_binding
    WHERE data_source_id = OLD.data_source_id
      AND external_track_id = OLD.external_id;
    RETURN OLD;
END;
$$ LANGUAGE plpgsql;

-- Trigger for album_binding
CREATE TRIGGER trg_cascade_delete_album_category_bindings
    BEFORE DELETE ON album_binding
    FOR EACH ROW
    EXECUTE FUNCTION fn_cascade_delete_album_category_bindings();

-- Trigger for track_binding
CREATE TRIGGER trg_cascade_delete_track_category_bindings
    BEFORE DELETE ON track_binding
    FOR EACH ROW
    EXECUTE FUNCTION fn_cascade_delete_track_category_bindings();
