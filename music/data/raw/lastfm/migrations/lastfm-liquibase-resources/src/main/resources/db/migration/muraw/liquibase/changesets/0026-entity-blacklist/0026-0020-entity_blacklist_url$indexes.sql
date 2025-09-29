-- Create unique constraint for entity_type and url combination
ALTER TABLE blacklist_entity_url
ADD CONSTRAINT blacklist_entity_url_UK_type_url UNIQUE (entity_type, url);

-- Create composite B-tree index for efficient blacklist lookups by entity_type and url
-- This index will be automatically created by the unique constraint above, but we add it explicitly for clarity
CREATE INDEX IF NOT EXISTS blacklist_entity_url_I_type_url
    ON blacklist_entity_url (entity_type, url);

-- Add comment
COMMENT ON INDEX blacklist_entity_url_I_type_url IS
    'Composite B-tree index for efficient blacklist lookups by entity type and URL. Supports both equality and IN operations.';
