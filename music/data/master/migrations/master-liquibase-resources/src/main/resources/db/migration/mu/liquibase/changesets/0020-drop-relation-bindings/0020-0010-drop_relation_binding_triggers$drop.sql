-- Drop cascade-delete triggers on entity binding tables that referenced relation binding tables.
-- These triggers were deleting rows from *_binding relation tables when entity bindings were removed.
-- Since the relation binding layer has been removed from the application, these triggers are no longer needed.

-- Triggers on artist_binding
DROP TRIGGER IF EXISTS trg_cascade_delete_artist_category_bindings ON artist_binding;
DROP TRIGGER IF EXISTS trg_cascade_delete_artist_track_bindings ON artist_binding;
DROP TRIGGER IF EXISTS trg_cascade_delete_artist_album_bindings ON artist_binding;

-- Triggers on album_binding
DROP TRIGGER IF EXISTS trg_cascade_delete_album_artist_bindings ON album_binding;
DROP TRIGGER IF EXISTS trg_cascade_delete_album_track_bindings ON album_binding;
DROP TRIGGER IF EXISTS trg_cascade_delete_album_category_bindings ON album_binding;

-- Triggers on category_binding
DROP TRIGGER IF EXISTS trg_cascade_delete_category_artist_bindings ON category_binding;

-- Triggers on track_binding
DROP TRIGGER IF EXISTS trg_cascade_delete_track_artist_bindings ON track_binding;
DROP TRIGGER IF EXISTS trg_cascade_delete_track_album_bindings ON track_binding;
DROP TRIGGER IF EXISTS trg_cascade_delete_track_category_bindings ON track_binding;
