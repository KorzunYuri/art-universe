-- Create dimension categories from existing dimensions
INSERT INTO category (id, name, created_at, updated_at)
SELECT 
    nextval('category_seq'),
    d.name,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM dimension d
ORDER BY d.id;

-- Update categories that have dimension_id but no parent_id to point to new dimension categories
UPDATE category 
SET parent_id = (
    SELECT c.id 
    FROM category c 
    JOIN dimension d ON d.name = c.name 
    WHERE d.id = category.dimension_id
)
WHERE dimension_id IS NOT NULL 
  AND parent_id IS NULL;
