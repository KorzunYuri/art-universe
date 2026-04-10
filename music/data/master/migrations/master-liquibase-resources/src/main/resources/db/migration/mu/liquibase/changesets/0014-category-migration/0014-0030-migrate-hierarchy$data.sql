-- Migrate all parent_id relationships to category_category table
INSERT INTO category_category (id, source_category_id, target_category_id, created_at, updated_at)
SELECT 
    nextval('category_category_seq'),
    parent_id,
    id,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM category 
WHERE parent_id IS NOT NULL;
