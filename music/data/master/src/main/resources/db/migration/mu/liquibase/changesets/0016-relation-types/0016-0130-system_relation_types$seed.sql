-- Seed system relation types

-- 1. "Is Primary Artist Of" — applies to ARTIST→ALBUM and ARTIST→TRACK
WITH inserted_type AS (
    INSERT INTO relation_type (id, name, reverse_name, is_symmetrical, is_system)
    VALUES (nextval('relation_type_seq'), 'Is Primary Artist Of', 'Has Primary Artist', FALSE, TRUE)
    RETURNING id
)
INSERT INTO relation_type_applicability (id, relation_type_id, source_entity_type, target_entity_type, is_default)
SELECT nextval('relation_type_applicability_seq'), inserted_type.id, source_type, target_type, TRUE
FROM inserted_type,
     (VALUES (1, 2), (1, 3)) AS pairs(source_type, target_type);

-- 2. "Contains" — applies to ALBUM→TRACK
WITH inserted_type AS (
    INSERT INTO relation_type (id, name, reverse_name, is_symmetrical, is_system)
    VALUES (nextval('relation_type_seq'), 'Contains', 'Is Contained In', FALSE, TRUE)
    RETURNING id
)
INSERT INTO relation_type_applicability (id, relation_type_id, source_entity_type, target_entity_type, is_default)
VALUES (nextval('relation_type_applicability_seq'), (SELECT id FROM inserted_type), 2, 3, TRUE);
