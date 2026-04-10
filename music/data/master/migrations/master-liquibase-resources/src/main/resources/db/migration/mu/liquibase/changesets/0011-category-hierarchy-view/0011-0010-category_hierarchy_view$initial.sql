-- Create materialized view for category hierarchy
CREATE MATERIALIZED VIEW mv_category_hierarchy AS
WITH RECURSIVE category_hierarchy_cte AS (
    -- Base case: root categories (no parent)
    SELECT
        c.id,
        c.name,
        c.dimension_id,
        c.dimension_id AS effective_dimension_id,
        c.parent_id,
        1 AS hierarchy_level
    FROM
        category c
    WHERE
        c.parent_id IS NULL
    
    UNION ALL
    
    -- Recursive case: child categories
    SELECT
        c.id,
        c.name,
        c.dimension_id,
        COALESCE(c.dimension_id, ch.effective_dimension_id) AS effective_dimension_id,
        c.parent_id,
        ch.hierarchy_level + 1 AS hierarchy_level
    FROM
        category c
    JOIN
        category_hierarchy_cte ch ON c.parent_id = ch.id
)
SELECT
    ch.id,
    ch.name,
    ch.dimension_id,
    ch.effective_dimension_id,
    ch.parent_id,
    ch.hierarchy_level
FROM
    category_hierarchy_cte ch;

-- Create index on the materialized view for faster lookups
CREATE INDEX idx_category_hierarchy_id ON mv_category_hierarchy(id);
CREATE INDEX idx_category_hierarchy_name ON mv_category_hierarchy(name);
CREATE INDEX idx_category_hierarchy_parent_id ON mv_category_hierarchy(parent_id);

-- Create function to refresh the materialized view
CREATE OR REPLACE FUNCTION fn_refresh_mv_category_hierarchy()
RETURNS TRIGGER AS $$
BEGIN
    REFRESH MATERIALIZED VIEW mv_category_hierarchy;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

-- Create trigger to refresh the materialized view when category table changes
CREATE TRIGGER trg_refresh_mv_category_hierarchy
AFTER INSERT OR UPDATE OR DELETE ON category
FOR EACH STATEMENT
EXECUTE FUNCTION fn_refresh_mv_category_hierarchy();
