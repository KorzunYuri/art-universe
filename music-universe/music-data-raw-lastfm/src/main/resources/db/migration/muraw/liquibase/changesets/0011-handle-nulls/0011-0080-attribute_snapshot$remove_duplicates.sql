WITH numbered AS (
    SELECT id,
           ROW_NUMBER() OVER (
               PARTITION BY scope_entity_type, scope_entity_id, attribute_id
               ORDER BY created_at DESC
               ) as rn
    FROM attribute_snapshot
    WHERE scope_entity_type IS NULL
      AND scope_entity_id IS NULL
)
DELETE FROM attribute_snapshot
WHERE id IN (
    SELECT id FROM numbered WHERE rn > 1
);