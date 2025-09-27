-- Drop old columns from category table
ALTER TABLE category DROP COLUMN IF EXISTS dimension_id;
ALTER TABLE category DROP COLUMN IF EXISTS parent_id;

-- Drop dimension table
DROP TABLE IF EXISTS dimension CASCADE;

-- Drop dimension sequence
DROP SEQUENCE IF EXISTS dimension_seq;
