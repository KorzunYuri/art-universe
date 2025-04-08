ALTER TABLE tag ALTER COLUMN usage_count DROP DEFAULT;
ALTER TABLE tag ALTER COLUMN usage_count DROP NOT NULL;
UPDATE tag SET usage_count = NULL where usage_count = 0;