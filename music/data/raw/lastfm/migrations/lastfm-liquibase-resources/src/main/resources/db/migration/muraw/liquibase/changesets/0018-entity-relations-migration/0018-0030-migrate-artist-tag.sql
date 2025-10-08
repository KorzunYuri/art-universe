INSERT INTO artist_tag (
    artist_id,
    tag_id,
    usage_count,
    api_call_id,
    created_at,
    updated_at
)
SELECT
    COALESCE(er.artist_id, ah.artist_id) as artist_id,
    COALESCE(er.tag_id, ah.tag_id) as tag_id,
    ah.usage_count,
    COALESCE(er.api_call_id, ah.api_call_id),
    COALESCE(er.created_at, ah.created_at),
    COALESCE(er.updated_at, ah.updated_at)
FROM
    (
        SELECT
            scope_entity_id AS tag_id,
            entity_id       AS artist_id,
            api_call_id,
            created_at,
            updated_at
        FROM
            entity_relation er
        WHERE   1=1
            AND er.scope_entity_type 	= 4 -- TAG
            AND er.entity_type 		    = 1 -- ARTIST
    ) er

FULL OUTER JOIN
     (
         SELECT
             scope_entity_id            AS artist_id,
             entity_id                  AS tag_id,
             int_value                  AS usage_count,
             api_call_id,
             valid_FROM                 AS created_at,
             valid_FROM                 AS updated_at
         FROM
             attribute_history
         where 1=1
           and scope_entity_type 	= 1  -- artist
           and entity_type 		    = 4  -- tag
           and attribute_id 		= 2  -- usage_count
           and valid_till 			= '9999-12-31'::date
     ) ah
     ON 	1=1
         and er.artist_id 	= ah.artist_id
         and er.tag_id 		= ah.tag_id
