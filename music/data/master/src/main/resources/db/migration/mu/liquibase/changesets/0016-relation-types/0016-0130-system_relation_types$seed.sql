-- Seed system relation types

-- 1. "Is Primary Artist Of" — applies to ARTIST→ALBUM and ARTIST→TRACK
INSERT INTO relation_type (id, name, reverse_name, is_symmetrical, is_system)
VALUES (nextval('relation_type_seq'), 'Is Primary Artist Of', 'Has Primary Artist', FALSE, TRUE)
ON CONFLICT (name) DO NOTHING;

INSERT INTO relation_type_applicability (id, relation_type_id, source_entity_type, target_entity_type, is_default)
SELECT nextval('relation_type_applicability_seq'), rt.id, pairs.source_type, pairs.target_type, TRUE
FROM relation_type rt
CROSS JOIN (VALUES (1, 2), (1, 3)) AS pairs(source_type, target_type)
WHERE rt.name = 'Is Primary Artist Of'
ON CONFLICT ON CONSTRAINT rta_UK DO NOTHING;

-- 2. "Contains" — applies to ALBUM→TRACK
INSERT INTO relation_type (id, name, reverse_name, is_symmetrical, is_system)
VALUES (nextval('relation_type_seq'), 'Contains', 'Is Contained In', FALSE, TRUE)
ON CONFLICT (name) DO NOTHING;

INSERT INTO relation_type_applicability (id, relation_type_id, source_entity_type, target_entity_type, is_default)
SELECT nextval('relation_type_applicability_seq'), rt.id, 2, 3, TRUE
FROM relation_type rt
WHERE rt.name = 'Contains'
ON CONFLICT ON CONSTRAINT rta_UK DO NOTHING;
