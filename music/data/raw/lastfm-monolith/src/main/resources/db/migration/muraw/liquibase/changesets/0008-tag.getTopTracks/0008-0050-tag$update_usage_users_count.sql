WITH ah AS (
    SELECT
            entity_id
        ,   int_value as usage_users_count
    FROM attribute_history
    WHERE   attribute_id = 2 -- reach
        and entity_type = 4
        and scope_entity_type   is null
        -- and scope_entity_id     is null -- TODO zero id is to be fixed
        and valid_till = '9999-12-31'
)
UPDATE tag
SET usage_users_count = ah.usage_users_count
FROM ah
WHERE id = ah.entity_id;