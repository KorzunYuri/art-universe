-- Drop trigger and function before dropping materialized view
DROP TRIGGER IF EXISTS trg_refresh_mv_category_hierarchy ON category;
DROP FUNCTION IF EXISTS fn_refresh_mv_category_hierarchy();

-- Drop dependent views and materialized views before dropping columns
DROP VIEW IF EXISTS mu_view.v_category_hierarchy CASCADE;
DROP VIEW IF EXISTS mu_view.v_category CASCADE;
DROP MATERIALIZED VIEW IF EXISTS mv_category_hierarchy CASCADE;
