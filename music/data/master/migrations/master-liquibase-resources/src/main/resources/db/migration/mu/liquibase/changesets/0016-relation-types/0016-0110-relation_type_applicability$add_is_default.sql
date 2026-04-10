-- Add is_default column to relation_type_applicability
ALTER TABLE relation_type_applicability
    ADD COLUMN IF NOT EXISTS is_default BOOLEAN NOT NULL DEFAULT FALSE;

-- Ensure at most one default relation type per entity pair
CREATE UNIQUE INDEX IF NOT EXISTS uk_rta_default
    ON relation_type_applicability(source_entity_type, target_entity_type)
    WHERE is_default = TRUE;
