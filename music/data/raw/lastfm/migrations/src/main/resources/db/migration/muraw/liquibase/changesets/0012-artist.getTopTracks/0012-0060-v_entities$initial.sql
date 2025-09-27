CREATE OR REPLACE VIEW v_entities AS
SELECT
        dict.code   as entity_type
    ,   dict."name" as entity_type_name
    ,   e.id        as entity_id
    ,   e.name      as entity_name
FROM
    tag as e
JOIN
    dictionary as dict
        on  1=1
            and dict.domain = 'LastfmEntityType'
            and dict.code   = 4

UNION ALL

SELECT
    dict.code
     ,   dict."name"
     ,   e.id
     ,   e.name
FROM
    artist as e
        JOIN
    dictionary as dict
    on  1=1
        and dict.domain = 'LastfmEntityType'
        and dict.code   = 1

UNION ALL

SELECT
    dict.code
     ,   dict."name"
     ,   e.id
     ,   e.name
FROM
    track as e
        JOIN
    dictionary as dict
    on  1=1
        and dict.domain = 'LastfmEntityType'
        and dict.code   = 3
;