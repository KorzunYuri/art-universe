INSERT INTO artist_artist (
    source_artist_id,
    target_artist_id,
    match_score,
    relation_type,
    api_call_id,
    created_at,
    updated_at
)
SELECT
    COALESCE(er.scope_entity_id, ah.scope_entity_id) as scope_entity_id,
    COALESCE(er.entity_id, ah.entity_id) as entity_id,
    ah.match_score,
    1 AS relation_type,     -- SIMILARITY
    COALESCE(er.api_call_id, ah.api_call_id),
    COALESCE(er.created_at, ah.created_at),
    COALESCE(er.updated_at, ah.updated_at)
FROM 
    (
        SELECT
            scope_entity_id,
            entity_id,
            api_call_id,
            created_at,
            updated_at
        FROM    
            entity_relation er
        WHERE   1=1
            AND er.scope_entity_type 	= 1 -- ARTIST
            AND er.entity_type 		    = 1 -- ARTIST
    ) er
FULL OUTER JOIN
    (
        SELECT
            scope_entity_id,
            entity_id,
            int_value::decimal / 100    AS match_score,
            api_call_id,
            valid_FROM                  AS created_at,
            valid_FROM                  AS updated_at
        FROM
            attribute_history
        WHERE  1=1
            AND scope_entity_type 	= 1  -- artist
            AND entity_type 		= 1  -- artist
            AND attribute_id 		= 11 -- match
            AND valid_till 			= '9999-12-31'::date
    ) ah
        ON  1=1
            AND er.scope_entity_id 	= ah.scope_entity_id
            AND er.entity_id 		= ah.entity_id