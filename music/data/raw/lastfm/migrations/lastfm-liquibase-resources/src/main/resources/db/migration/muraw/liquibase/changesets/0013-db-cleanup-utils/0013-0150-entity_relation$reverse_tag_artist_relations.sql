-- remove duplicates
DELETE FROM entity_relation r1
WHERE   r1.scope_entity_type = 1
    AND r1.entity_type = 4
    AND EXISTS (
        SELECT  1
        FROM    entity_relation r2
        WHERE   r2.scope_entity_type    = 4
            AND r2.entity_type          = 1
            AND r2.scope_entity_id      = r1.entity_id
            AND r2.entity_id            = r1.scope_entity_id
);

-- reverse relations
UPDATE entity_relation
SET scope_entity_type   = entity_type,
    scope_entity_id     = entity_id,
    entity_type         = scope_entity_type,
    entity_id           = scope_entity_id
WHERE   scope_entity_type   = 1  -- artist
    and entity_type         = 4  -- tag
;