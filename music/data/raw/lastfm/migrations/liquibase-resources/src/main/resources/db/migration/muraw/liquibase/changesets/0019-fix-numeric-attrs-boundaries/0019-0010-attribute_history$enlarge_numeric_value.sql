ALTER TABLE attribute_history ALTER COLUMN int_value TYPE BIGINT;
ALTER TABLE attribute_history RENAME COLUMN int_value TO numeric_value;
