INSERT INTO track_tag (
    track_id,
    tag_id,
    api_call_id,
    created_at,
    updated_at
)
SELECT
    er.entity_id as track_id,
    er.scope_entity_id as tag_id,
    er.api_call_id,
    er.created_at,
    er.updated_at
FROM
    entity_relation er
WHERE   1=1
    AND er.scope_entity_type 	= 4 -- TAG
    AND er.entity_type 		    = 3 -- TRACK
