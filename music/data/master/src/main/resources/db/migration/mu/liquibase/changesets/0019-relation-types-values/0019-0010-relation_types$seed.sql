-- Seed relation types for the music domain (ID range 1003-1999)
-- System types 1001-1002 are seeded in 0016-0130.
-- Entity type codes: ARTIST=1, ALBUM=2, TRACK=3, CATEGORY=4, PERSON=101
-- Applicability ID = relation_type_id * 1000 + sequential order

-- =============================================================================
-- Artist ↔ Person relation types (1003-1006)
-- =============================================================================

-- 1003. "Has Member" / "Is Member Of" — ARTIST→PERSON (default)
INSERT INTO relation_type (id, name, reverse_name, is_symmetrical, is_system)
VALUES (1003, 'Has Member', 'Is Member Of', FALSE, FALSE)
ON CONFLICT (name) DO NOTHING;

INSERT INTO relation_type_applicability (id, relation_type_id, source_entity_type, target_entity_type, is_default)
VALUES (1003001, 1003, 1, 101, TRUE)
ON CONFLICT ON CONSTRAINT rta_UK DO NOTHING;

-- 1004. "Is Persona Of" / "Performs As" — ARTIST→PERSON
INSERT INTO relation_type (id, name, reverse_name, is_symmetrical, is_system)
VALUES (1004, 'Is Persona Of', 'Performs As', FALSE, FALSE)
ON CONFLICT (name) DO NOTHING;

INSERT INTO relation_type_applicability (id, relation_type_id, source_entity_type, target_entity_type, is_default)
VALUES (1004001, 1004, 1, 101, FALSE)
ON CONFLICT ON CONSTRAINT rta_UK DO NOTHING;

-- 1005. "Founded By" / "Is Founder Of" — ARTIST→PERSON
INSERT INTO relation_type (id, name, reverse_name, is_symmetrical, is_system)
VALUES (1005, 'Founded By', 'Is Founder Of', FALSE, FALSE)
ON CONFLICT (name) DO NOTHING;

INSERT INTO relation_type_applicability (id, relation_type_id, source_entity_type, target_entity_type, is_default)
VALUES (1005001, 1005, 1, 101, FALSE)
ON CONFLICT ON CONSTRAINT rta_UK DO NOTHING;

-- 1006. "Has Session Musician" / "Session Musician For" — ARTIST→PERSON
INSERT INTO relation_type (id, name, reverse_name, is_symmetrical, is_system)
VALUES (1006, 'Has Session Musician', 'Session Musician For', FALSE, FALSE)
ON CONFLICT (name) DO NOTHING;

INSERT INTO relation_type_applicability (id, relation_type_id, source_entity_type, target_entity_type, is_default)
VALUES (1006001, 1006, 1, 101, FALSE)
ON CONFLICT ON CONSTRAINT rta_UK DO NOTHING;


-- =============================================================================
-- Album ↔ Person relation types (1007-1012)
-- =============================================================================

-- 1007. "Produced By" / "Produced" — ALBUM→PERSON (default), TRACK→PERSON
INSERT INTO relation_type (id, name, reverse_name, is_symmetrical, is_system)
VALUES (1007, 'Produced By', 'Produced', FALSE, FALSE)
ON CONFLICT (name) DO NOTHING;

INSERT INTO relation_type_applicability (id, relation_type_id, source_entity_type, target_entity_type, is_default)
VALUES (1007001, 1007, 2, 101, TRUE)
ON CONFLICT ON CONSTRAINT rta_UK DO NOTHING;

INSERT INTO relation_type_applicability (id, relation_type_id, source_entity_type, target_entity_type, is_default)
VALUES (1007002, 1007, 3, 101, FALSE)
ON CONFLICT ON CONSTRAINT rta_UK DO NOTHING;

-- 1008. "Engineered By" / "Engineered" — ALBUM→PERSON
INSERT INTO relation_type (id, name, reverse_name, is_symmetrical, is_system)
VALUES (1008, 'Engineered By', 'Engineered', FALSE, FALSE)
ON CONFLICT (name) DO NOTHING;

INSERT INTO relation_type_applicability (id, relation_type_id, source_entity_type, target_entity_type, is_default)
VALUES (1008001, 1008, 2, 101, FALSE)
ON CONFLICT ON CONSTRAINT rta_UK DO NOTHING;

-- 1009. "Mixed By" / "Mixed" — ALBUM→PERSON, TRACK→PERSON
INSERT INTO relation_type (id, name, reverse_name, is_symmetrical, is_system)
VALUES (1009, 'Mixed By', 'Mixed', FALSE, FALSE)
ON CONFLICT (name) DO NOTHING;

INSERT INTO relation_type_applicability (id, relation_type_id, source_entity_type, target_entity_type, is_default)
VALUES (1009001, 1009, 2, 101, FALSE)
ON CONFLICT ON CONSTRAINT rta_UK DO NOTHING;

INSERT INTO relation_type_applicability (id, relation_type_id, source_entity_type, target_entity_type, is_default)
VALUES (1009002, 1009, 3, 101, FALSE)
ON CONFLICT ON CONSTRAINT rta_UK DO NOTHING;

-- 1010. "Mastered By" / "Mastered" — ALBUM→PERSON
INSERT INTO relation_type (id, name, reverse_name, is_symmetrical, is_system)
VALUES (1010, 'Mastered By', 'Mastered', FALSE, FALSE)
ON CONFLICT (name) DO NOTHING;

INSERT INTO relation_type_applicability (id, relation_type_id, source_entity_type, target_entity_type, is_default)
VALUES (1010001, 1010, 2, 101, FALSE)
ON CONFLICT ON CONSTRAINT rta_UK DO NOTHING;

-- 1011. "Executive Produced By" / "Executive Produced" — ALBUM→PERSON
INSERT INTO relation_type (id, name, reverse_name, is_symmetrical, is_system)
VALUES (1011, 'Executive Produced By', 'Executive Produced', FALSE, FALSE)
ON CONFLICT (name) DO NOTHING;

INSERT INTO relation_type_applicability (id, relation_type_id, source_entity_type, target_entity_type, is_default)
VALUES (1011001, 1011, 2, 101, FALSE)
ON CONFLICT ON CONSTRAINT rta_UK DO NOTHING;

-- 1012. "Cover Designed By" / "Designed Cover For" — ALBUM→PERSON
INSERT INTO relation_type (id, name, reverse_name, is_symmetrical, is_system)
VALUES (1012, 'Cover Designed By', 'Designed Cover For', FALSE, FALSE)
ON CONFLICT (name) DO NOTHING;

INSERT INTO relation_type_applicability (id, relation_type_id, source_entity_type, target_entity_type, is_default)
VALUES (1012001, 1012, 2, 101, FALSE)
ON CONFLICT ON CONSTRAINT rta_UK DO NOTHING;


-- =============================================================================
-- Track ↔ Person relation types (1013-1016)
-- =============================================================================

-- 1013. "Written By" / "Wrote" — TRACK→PERSON (default)
INSERT INTO relation_type (id, name, reverse_name, is_symmetrical, is_system)
VALUES (1013, 'Written By', 'Wrote', FALSE, FALSE)
ON CONFLICT (name) DO NOTHING;

INSERT INTO relation_type_applicability (id, relation_type_id, source_entity_type, target_entity_type, is_default)
VALUES (1013001, 1013, 3, 101, TRUE)
ON CONFLICT ON CONSTRAINT rta_UK DO NOTHING;

-- 1014. "Composed By" / "Composed" — TRACK→PERSON
INSERT INTO relation_type (id, name, reverse_name, is_symmetrical, is_system)
VALUES (1014, 'Composed By', 'Composed', FALSE, FALSE)
ON CONFLICT (name) DO NOTHING;

INSERT INTO relation_type_applicability (id, relation_type_id, source_entity_type, target_entity_type, is_default)
VALUES (1014001, 1014, 3, 101, FALSE)
ON CONFLICT ON CONSTRAINT rta_UK DO NOTHING;

-- 1015. "Arranged By" / "Arranged" — TRACK→PERSON
INSERT INTO relation_type (id, name, reverse_name, is_symmetrical, is_system)
VALUES (1015, 'Arranged By', 'Arranged', FALSE, FALSE)
ON CONFLICT (name) DO NOTHING;

INSERT INTO relation_type_applicability (id, relation_type_id, source_entity_type, target_entity_type, is_default)
VALUES (1015001, 1015, 3, 101, FALSE)
ON CONFLICT ON CONSTRAINT rta_UK DO NOTHING;

-- 1016. "Features" / "Featured On" — TRACK→PERSON
INSERT INTO relation_type (id, name, reverse_name, is_symmetrical, is_system)
VALUES (1016, 'Features', 'Featured On', FALSE, FALSE)
ON CONFLICT (name) DO NOTHING;

INSERT INTO relation_type_applicability (id, relation_type_id, source_entity_type, target_entity_type, is_default)
VALUES (1016001, 1016, 3, 101, FALSE)
ON CONFLICT ON CONSTRAINT rta_UK DO NOTHING;


-- =============================================================================
-- Person ↔ Person relation types (1017-1020)
-- =============================================================================

-- 1017. "Married To" — PERSON→PERSON, symmetrical
INSERT INTO relation_type (id, name, reverse_name, is_symmetrical, is_system)
VALUES (1017, 'Married To', 'Married To', TRUE, FALSE)
ON CONFLICT (name) DO NOTHING;

INSERT INTO relation_type_applicability (id, relation_type_id, source_entity_type, target_entity_type, is_default)
VALUES (1017001, 1017, 101, 101, FALSE)
ON CONFLICT ON CONSTRAINT rta_UK DO NOTHING;

-- 1018. "Sibling Of" — PERSON→PERSON, symmetrical
INSERT INTO relation_type (id, name, reverse_name, is_symmetrical, is_system)
VALUES (1018, 'Sibling Of', 'Sibling Of', TRUE, FALSE)
ON CONFLICT (name) DO NOTHING;

INSERT INTO relation_type_applicability (id, relation_type_id, source_entity_type, target_entity_type, is_default)
VALUES (1018001, 1018, 101, 101, FALSE)
ON CONFLICT ON CONSTRAINT rta_UK DO NOTHING;

-- 1019. "Parent Of" / "Child Of" — PERSON→PERSON
INSERT INTO relation_type (id, name, reverse_name, is_symmetrical, is_system)
VALUES (1019, 'Parent Of', 'Child Of', FALSE, FALSE)
ON CONFLICT (name) DO NOTHING;

INSERT INTO relation_type_applicability (id, relation_type_id, source_entity_type, target_entity_type, is_default)
VALUES (1019001, 1019, 101, 101, FALSE)
ON CONFLICT ON CONSTRAINT rta_UK DO NOTHING;

-- 1020. "Mentored" / "Mentored By" — PERSON→PERSON
INSERT INTO relation_type (id, name, reverse_name, is_symmetrical, is_system)
VALUES (1020, 'Mentored', 'Mentored By', FALSE, FALSE)
ON CONFLICT (name) DO NOTHING;

INSERT INTO relation_type_applicability (id, relation_type_id, source_entity_type, target_entity_type, is_default)
VALUES (1020001, 1020, 101, 101, FALSE)
ON CONFLICT ON CONSTRAINT rta_UK DO NOTHING;


-- =============================================================================
-- Artist ↔ Artist relation types (1021-1026)
-- =============================================================================

-- 1021. "Collaborates With" — ARTIST→ARTIST, symmetrical
INSERT INTO relation_type (id, name, reverse_name, is_symmetrical, is_system)
VALUES (1021, 'Collaborates With', NULL, TRUE, FALSE)
ON CONFLICT (name) DO NOTHING;

INSERT INTO relation_type_applicability (id, relation_type_id, source_entity_type, target_entity_type, is_default)
VALUES (1021001, 1021, 1, 1, FALSE)
ON CONFLICT ON CONSTRAINT rta_UK DO NOTHING;

-- 1022. "Similar To" — ARTIST→ARTIST, symmetrical
INSERT INTO relation_type (id, name, reverse_name, is_symmetrical, is_system)
VALUES (1022, 'Similar To', 'Similar To', TRUE, FALSE)
ON CONFLICT (name) DO NOTHING;

INSERT INTO relation_type_applicability (id, relation_type_id, source_entity_type, target_entity_type, is_default)
VALUES (1022001, 1022, 1, 1, FALSE)
ON CONFLICT ON CONSTRAINT rta_UK DO NOTHING;

-- 1023. "Influenced By" / "Influenced" — ARTIST→ARTIST
INSERT INTO relation_type (id, name, reverse_name, is_symmetrical, is_system)
VALUES (1023, 'Influenced By', 'Influenced', FALSE, FALSE)
ON CONFLICT (name) DO NOTHING;

INSERT INTO relation_type_applicability (id, relation_type_id, source_entity_type, target_entity_type, is_default)
VALUES (1023001, 1023, 1, 1, FALSE)
ON CONFLICT ON CONSTRAINT rta_UK DO NOTHING;

-- 1024. "Side Project Of" / "Has Side Project" — ARTIST→ARTIST
INSERT INTO relation_type (id, name, reverse_name, is_symmetrical, is_system)
VALUES (1024, 'Side Project Of', 'Has Side Project', FALSE, FALSE)
ON CONFLICT (name) DO NOTHING;

INSERT INTO relation_type_applicability (id, relation_type_id, source_entity_type, target_entity_type, is_default)
VALUES (1024001, 1024, 1, 1, FALSE)
ON CONFLICT ON CONSTRAINT rta_UK DO NOTHING;

-- 1025. "Successor Of" / "Predecessor Of" — ARTIST→ARTIST
INSERT INTO relation_type (id, name, reverse_name, is_symmetrical, is_system)
VALUES (1025, 'Successor Of', 'Predecessor Of', FALSE, FALSE)
ON CONFLICT (name) DO NOTHING;

INSERT INTO relation_type_applicability (id, relation_type_id, source_entity_type, target_entity_type, is_default)
VALUES (1025001, 1025, 1, 1, FALSE)
ON CONFLICT ON CONSTRAINT rta_UK DO NOTHING;

-- 1026. "Split Into" / "Split From" — ARTIST→ARTIST
INSERT INTO relation_type (id, name, reverse_name, is_symmetrical, is_system)
VALUES (1026, 'Split Into', 'Split From', FALSE, FALSE)
ON CONFLICT (name) DO NOTHING;

INSERT INTO relation_type_applicability (id, relation_type_id, source_entity_type, target_entity_type, is_default)
VALUES (1026001, 1026, 1, 1, FALSE)
ON CONFLICT ON CONSTRAINT rta_UK DO NOTHING;


-- =============================================================================
-- Track ↔ Track relation types (1027-1031)
-- =============================================================================

-- 1027. "Samples" / "Sampled By" — TRACK→TRACK
INSERT INTO relation_type (id, name, reverse_name, is_symmetrical, is_system)
VALUES (1027, 'Samples', 'Sampled By', FALSE, FALSE)
ON CONFLICT (name) DO NOTHING;

INSERT INTO relation_type_applicability (id, relation_type_id, source_entity_type, target_entity_type, is_default)
VALUES (1027001, 1027, 3, 3, FALSE)
ON CONFLICT ON CONSTRAINT rta_UK DO NOTHING;

-- 1028. "Remix Of" / "Has Remix" — TRACK→TRACK
INSERT INTO relation_type (id, name, reverse_name, is_symmetrical, is_system)
VALUES (1028, 'Remix Of', 'Has Remix', FALSE, FALSE)
ON CONFLICT (name) DO NOTHING;

INSERT INTO relation_type_applicability (id, relation_type_id, source_entity_type, target_entity_type, is_default)
VALUES (1028001, 1028, 3, 3, FALSE)
ON CONFLICT ON CONSTRAINT rta_UK DO NOTHING;

-- 1029. "Cover Of" / "Has Cover" — TRACK→TRACK
INSERT INTO relation_type (id, name, reverse_name, is_symmetrical, is_system)
VALUES (1029, 'Cover Of', 'Has Cover', FALSE, FALSE)
ON CONFLICT (name) DO NOTHING;

INSERT INTO relation_type_applicability (id, relation_type_id, source_entity_type, target_entity_type, is_default)
VALUES (1029001, 1029, 3, 3, FALSE)
ON CONFLICT ON CONSTRAINT rta_UK DO NOTHING;

-- 1030. "Live Version Of" / "Has Live Version" — TRACK→TRACK
INSERT INTO relation_type (id, name, reverse_name, is_symmetrical, is_system)
VALUES (1030, 'Live Version Of', 'Has Live Version', FALSE, FALSE)
ON CONFLICT (name) DO NOTHING;

INSERT INTO relation_type_applicability (id, relation_type_id, source_entity_type, target_entity_type, is_default)
VALUES (1030001, 1030, 3, 3, FALSE)
ON CONFLICT ON CONSTRAINT rta_UK DO NOTHING;

-- 1031. "Sequel To" / "Has Sequel" — TRACK→TRACK, ALBUM→ALBUM
INSERT INTO relation_type (id, name, reverse_name, is_symmetrical, is_system)
VALUES (1031, 'Sequel To', 'Has Sequel', FALSE, FALSE)
ON CONFLICT (name) DO NOTHING;

INSERT INTO relation_type_applicability (id, relation_type_id, source_entity_type, target_entity_type, is_default)
VALUES (1031001, 1031, 3, 3, FALSE)
ON CONFLICT ON CONSTRAINT rta_UK DO NOTHING;

INSERT INTO relation_type_applicability (id, relation_type_id, source_entity_type, target_entity_type, is_default)
VALUES (1031002, 1031, 2, 2, FALSE)
ON CONFLICT ON CONSTRAINT rta_UK DO NOTHING;


-- =============================================================================
-- Album ↔ Album relation types (1032-1034)
-- =============================================================================

-- 1032. "Remaster Of" / "Has Remaster" — ALBUM→ALBUM
INSERT INTO relation_type (id, name, reverse_name, is_symmetrical, is_system)
VALUES (1032, 'Remaster Of', 'Has Remaster', FALSE, FALSE)
ON CONFLICT (name) DO NOTHING;

INSERT INTO relation_type_applicability (id, relation_type_id, source_entity_type, target_entity_type, is_default)
VALUES (1032001, 1032, 2, 2, FALSE)
ON CONFLICT ON CONSTRAINT rta_UK DO NOTHING;

-- 1033. "Deluxe Edition Of" / "Has Deluxe Edition" — ALBUM→ALBUM
INSERT INTO relation_type (id, name, reverse_name, is_symmetrical, is_system)
VALUES (1033, 'Deluxe Edition Of', 'Has Deluxe Edition', FALSE, FALSE)
ON CONFLICT (name) DO NOTHING;

INSERT INTO relation_type_applicability (id, relation_type_id, source_entity_type, target_entity_type, is_default)
VALUES (1033001, 1033, 2, 2, FALSE)
ON CONFLICT ON CONSTRAINT rta_UK DO NOTHING;

-- 1034. "Reissue Of" / "Has Reissue" — ALBUM→ALBUM
INSERT INTO relation_type (id, name, reverse_name, is_symmetrical, is_system)
VALUES (1034, 'Reissue Of', 'Has Reissue', FALSE, FALSE)
ON CONFLICT (name) DO NOTHING;

INSERT INTO relation_type_applicability (id, relation_type_id, source_entity_type, target_entity_type, is_default)
VALUES (1034001, 1034, 2, 2, FALSE)
ON CONFLICT ON CONSTRAINT rta_UK DO NOTHING;
