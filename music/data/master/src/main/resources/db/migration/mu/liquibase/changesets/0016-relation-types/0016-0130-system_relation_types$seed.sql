-- Seed system relation types

-- 1. "Is Primary Artist Of" — applies to ARTIST→ALBUM and ARTIST→TRACK
INSERT INTO relation_type (id, name, reverse_name, is_symmetrical, is_system)
VALUES (1001, 'Is Primary Artist Of', 'Has Primary Artist', FALSE, TRUE)
ON CONFLICT (name) DO NOTHING;

INSERT INTO relation_type_applicability (id, relation_type_id, source_entity_type, target_entity_type, is_default)
SELECT 1001001, rt.id, 1, 2, TRUE
FROM relation_type rt
WHERE rt.name = 'Is Primary Artist Of'
ON CONFLICT ON CONSTRAINT rta_UK DO NOTHING;

INSERT INTO relation_type_applicability (id, relation_type_id, source_entity_type, target_entity_type, is_default)
SELECT 1001002, rt.id, 1, 3, TRUE
FROM relation_type rt
WHERE rt.name = 'Is Primary Artist Of'
ON CONFLICT ON CONSTRAINT rta_UK DO NOTHING;

-- 2. "Contains" — applies to ALBUM→TRACK
INSERT INTO relation_type (id, name, reverse_name, is_symmetrical, is_system)
VALUES (1002, 'Contains', 'Is Contained In', FALSE, TRUE)
ON CONFLICT (name) DO NOTHING;

INSERT INTO relation_type_applicability (id, relation_type_id, source_entity_type, target_entity_type, is_default)
SELECT 1002001, rt.id, 2, 3, TRUE
FROM relation_type rt
WHERE rt.name = 'Contains'
ON CONFLICT ON CONSTRAINT rta_UK DO NOTHING;
