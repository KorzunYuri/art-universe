-- Create materialized view for category descendants (all children including transitive)
CREATE MATERIALIZED VIEW mv_category_children AS
WITH RECURSIVE category_children AS (
    -- Base case: direct parent-child relationships
    SELECT 
        cc.source_category_id as id,
        pc.name,
        cc.target_category_id as child_id,
        cc.target_category_id as descendant_id,
        c.name as child_name,
        ARRAY[cc.source_category_id, cc.target_category_id] as path
    FROM category_category cc
    JOIN category pc ON cc.source_category_id = pc.id
    JOIN category c ON cc.target_category_id = c.id
    
    UNION ALL
    
    -- Recursive case: transitive relationships
    SELECT 
        cd.id,
        cd.name,
        cc.target_category_id as child_id,
        cc.target_category_id as descendant_id,
        c.name as child_name,
        cd.path || cc.target_category_id
    FROM category_children cd
    JOIN category_category cc ON cd.descendant_id = cc.source_category_id
    JOIN category c ON cc.target_category_id = c.id
    WHERE NOT (cc.target_category_id = ANY(cd.path))  -- Prevent cycles
)
SELECT DISTINCT
    id,
    name,
    child_id,
    child_name
FROM category_children
ORDER BY id, child_id;

-- Create indexes for performance
CREATE INDEX mv_category_children_I_id ON mv_category_children(id);
CREATE INDEX mv_category_children_I_child_id ON mv_category_children(child_id);

-- Create function to refresh the materialized view
CREATE OR REPLACE FUNCTION fn_refresh_mv_category_children()
RETURNS TRIGGER AS $$
BEGIN
    REFRESH MATERIALIZED VIEW mv_category_children;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

-- Create triggers to refresh when category or category_category tables change
CREATE TRIGGER trg_refresh_mv_category_children_category
AFTER INSERT OR UPDATE OR DELETE ON category
FOR EACH STATEMENT
EXECUTE FUNCTION fn_refresh_mv_category_children();

CREATE TRIGGER trg_refresh_mv_category_children_relations
AFTER INSERT OR UPDATE OR DELETE ON category_category
FOR EACH STATEMENT
EXECUTE FUNCTION fn_refresh_mv_category_children();
