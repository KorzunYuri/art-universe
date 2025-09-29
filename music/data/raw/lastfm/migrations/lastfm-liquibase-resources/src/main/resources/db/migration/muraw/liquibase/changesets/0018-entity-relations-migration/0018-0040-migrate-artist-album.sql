INSERT INTO artist_album (
    artist_id,
    album_id,
    api_call_id,
    created_at,
    updated_at
)
SELECT
    er.scope_entity_id as artist_id,
    er.entity_id as album_id,
    er.api_call_id,
    er.created_at,
    er.updated_at
FROM
    entity_relation er
WHERE   1=1
    AND er.scope_entity_type 	= 1 -- ARTIST
    AND er.entity_type 		    = 2 -- ALBUM
