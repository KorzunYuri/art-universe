-- Function to delete artist_album_binding when artist_binding is deleted
CREATE OR REPLACE FUNCTION fn_cascade_delete_artist_album_bindings()
RETURNS TRIGGER AS $$
BEGIN
    DELETE FROM artist_album_binding
    WHERE data_source_id = OLD.data_source_id
      AND external_artist_id = OLD.external_id;
    RETURN OLD;
END;
$$ LANGUAGE plpgsql;

-- Function to delete artist_album_binding when album_binding is deleted
CREATE OR REPLACE FUNCTION fn_cascade_delete_album_artist_bindings()
RETURNS TRIGGER AS $$
BEGIN
    DELETE FROM artist_album_binding
    WHERE data_source_id = OLD.data_source_id
      AND external_album_id = OLD.external_id;
    RETURN OLD;
END;
$$ LANGUAGE plpgsql;

-- Function to delete album_track_binding when album_binding is deleted
CREATE OR REPLACE FUNCTION fn_cascade_delete_album_track_bindings()
RETURNS TRIGGER AS $$
BEGIN
    DELETE FROM album_track_binding
    WHERE data_source_id = OLD.data_source_id
      AND external_album_id = OLD.external_id;
    RETURN OLD;
END;
$$ LANGUAGE plpgsql;

-- Function to delete album_track_binding when track_binding is deleted
CREATE OR REPLACE FUNCTION fn_cascade_delete_track_album_bindings()
RETURNS TRIGGER AS $$
BEGIN
    DELETE FROM album_track_binding
    WHERE data_source_id = OLD.data_source_id
      AND external_track_id = OLD.external_id;
    RETURN OLD;
END;
$$ LANGUAGE plpgsql;

-- Triggers for artist_binding
CREATE TRIGGER trg_cascade_delete_artist_album_bindings
    BEFORE DELETE ON artist_binding
    FOR EACH ROW
    EXECUTE FUNCTION fn_cascade_delete_artist_album_bindings();

-- Triggers for album_binding
CREATE TRIGGER trg_cascade_delete_album_artist_bindings
    BEFORE DELETE ON album_binding
    FOR EACH ROW
    EXECUTE FUNCTION fn_cascade_delete_album_artist_bindings();

CREATE TRIGGER trg_cascade_delete_album_track_bindings
    BEFORE DELETE ON album_binding
    FOR EACH ROW
    EXECUTE FUNCTION fn_cascade_delete_album_track_bindings();

-- Trigger for track_binding
CREATE TRIGGER trg_cascade_delete_track_album_bindings
    BEFORE DELETE ON track_binding
    FOR EACH ROW
    EXECUTE FUNCTION fn_cascade_delete_track_album_bindings();
