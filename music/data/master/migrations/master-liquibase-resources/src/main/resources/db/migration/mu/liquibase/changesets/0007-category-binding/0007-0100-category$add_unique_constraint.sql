-- Add unique constraint on category name
ALTER TABLE category
    ADD CONSTRAINT category_name_uk UNIQUE (name);
