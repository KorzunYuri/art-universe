ALTER TABLE tag ALTER COLUMN usage_users_count DROP DEFAULT;
ALTER TABLE tag ALTER COLUMN usage_users_count DROP NOT NULL;
UPDATE tag SET usage_users_count = NULL where usage_users_count = 0;