-- Fix index names for category_category table to follow naming convention
DROP INDEX IF EXISTS idx_category_category_source_id;
DROP INDEX IF EXISTS idx_category_category_target_id;

CREATE INDEX category_category_I_source_id ON category_category(source_category_id);
CREATE INDEX category_category_I_target_id ON category_category(target_category_id);
