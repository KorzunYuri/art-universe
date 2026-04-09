-- Category view - only id and name (no dimension fields)
CREATE OR REPLACE VIEW mu_view.v_category AS
SELECT id, name FROM mu.category;

-- Category descendants view - expose the descendants view to readonly schema
CREATE OR REPLACE VIEW mu_view.v_category_children AS
SELECT id, name, child_id, child_name 
FROM mu.mv_category_children;

-- Grant SELECT permissions on new views
GRANT SELECT ON mu_view.v_category TO PUBLIC;
GRANT SELECT ON mu_view.v_category_children TO PUBLIC;
