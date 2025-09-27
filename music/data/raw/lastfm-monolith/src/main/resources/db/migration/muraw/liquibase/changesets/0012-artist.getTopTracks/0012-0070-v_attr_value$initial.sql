CREATE OR REPLACE VIEW v_attr_value AS
SELECT  attr.id                 AS attribute_id
    ,   attr.name               AS attribute_name
    ,   ah.scope_entity_type
    ,   se.entity_type_name     AS scope_entity_type_name
    ,   ah.scope_entity_id
    ,   se.entity_name          AS scope_entity_name
    ,   ah.entity_type
    ,   e.entity_type_name
    ,   ah.entity_id
    ,   e.entity_name
    ,   ah.int_value
    ,   ah.string_value
    ,   ah.collection_ts
    ,   ah.valid_from
    ,   ah.valid_till
FROM
    attribute_history ah
JOIN
    v_entities e
        ON      e.entity_type   = ah.entity_type
            AND e.entity_id     = ah.entity_id
LEFT JOIN
    v_entities se
    ON      se.entity_type   = ah.scope_entity_type
        AND se.entity_id     = ah.scope_entity_id
JOIN attribute attr
    ON      attr.id = ah.attribute_id;