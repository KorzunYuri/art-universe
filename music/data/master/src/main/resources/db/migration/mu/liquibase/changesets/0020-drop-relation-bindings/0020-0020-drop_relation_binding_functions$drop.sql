-- Drop cascade-delete functions that were used by the relation binding triggers.

-- Functions from 0012 (artist_category_binding, artist_track_binding)
DROP FUNCTION IF EXISTS fn_cascade_delete_artist_category_bindings();
DROP FUNCTION IF EXISTS fn_cascade_delete_artist_track_bindings();
DROP FUNCTION IF EXISTS fn_cascade_delete_category_artist_bindings();
DROP FUNCTION IF EXISTS fn_cascade_delete_track_artist_bindings();

-- Functions from 0016 (artist_album_binding, album_track_binding)
DROP FUNCTION IF EXISTS fn_cascade_delete_artist_album_bindings();
DROP FUNCTION IF EXISTS fn_cascade_delete_album_artist_bindings();
DROP FUNCTION IF EXISTS fn_cascade_delete_album_track_bindings();
DROP FUNCTION IF EXISTS fn_cascade_delete_track_album_bindings();

-- Functions from 0017 (album_category_binding, track_category_binding)
DROP FUNCTION IF EXISTS fn_cascade_delete_album_category_bindings();
DROP FUNCTION IF EXISTS fn_cascade_delete_track_category_bindings();
