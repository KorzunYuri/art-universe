-- Function to delete artist_category_binding when artist_binding is deleted
CREATE OR REPLACE FUNCTION fn_cascade_delete_artist_category_bindings()
RETURNS TRIGGER AS $$
BEGIN
    DELETE FROM artist_category_binding 
    WHERE data_source_id = OLD.data_source_id 
      AND external_artist_id = OLD.external_id;
    RETURN OLD;
END;
$$ LANGUAGE plpgsql;

-- Function to delete artist_track_binding when artist_binding is deleted
CREATE OR REPLACE FUNCTION fn_cascade_delete_artist_track_bindings()
RETURNS TRIGGER AS $$
BEGIN
    DELETE FROM artist_track_binding 
    WHERE data_source_id = OLD.data_source_id 
      AND external_artist_id = OLD.external_id;
    RETURN OLD;
END;
$$ LANGUAGE plpgsql;

-- Function to delete artist_category_binding when category_binding is deleted
CREATE OR REPLACE FUNCTION fn_cascade_delete_category_artist_bindings()
RETURNS TRIGGER AS $$
BEGIN
    DELETE FROM artist_category_binding 
    WHERE data_source_id = OLD.data_source_id 
      AND external_category_id = OLD.external_id;
    RETURN OLD;
END;
$$ LANGUAGE plpgsql;

-- Function to delete artist_track_binding when track_binding is deleted
CREATE OR REPLACE FUNCTION fn_cascade_delete_track_artist_bindings()
RETURNS TRIGGER AS $$
BEGIN
    DELETE FROM artist_track_binding 
    WHERE data_source_id = OLD.data_source_id 
      AND external_track_id = OLD.external_id;
    RETURN OLD;
END;
$$ LANGUAGE plpgsql;
