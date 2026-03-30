-- Seed SEMANTIC attribute definitions (computation_type=4)
-- These attributes are extracted by LLM-driven semantic analysis from unstructured text
-- (artist biographies, wiki content) and tag clouds.
--
-- Data types: NUMERIC(1), STRING(2), DATE(3), BOOLEAN(4)
-- Temporal types: CONSTANT(1), INSTANT(2), PERIOD(3)
-- Target types: ARTIST(1)

-- === SEMANTIC attributes (301-304) ===
INSERT INTO attribute_def (id, code, name, description, data_type, temporal_type, computation_type, data_source, is_system) VALUES
    (301, 'country_of_origin',  'Country of Origin',
        'Country where the artist was formed or originates from, extracted from biography text',
        2, 1, 4, NULL, true),
    (302, 'city_of_origin',     'City of Origin',
        'City/region where the artist was formed or originates from, extracted from biography text',
        2, 1, 4, NULL, true),
    (303, 'founding_year',      'Founding Year',
        'Year the artist/band was founded or began their career, extracted from biography text',
        1, 2, 4, NULL, true),
    (304, 'activity_periods',   'Activity Periods',
        'Periods of activity and hiatus. Each value row represents one period with valid_from/valid_to timestamps and value indicating status (active/hiatus)',
        2, 3, 4, NULL, true);

UPDATE attribute_def SET is_multi_value = true WHERE id = 304;

-- === Applicability: all apply to ARTIST ===
INSERT INTO attribute_def_applicability (id, attribute_def_id, target_type) VALUES
    (301, 301, 1),  -- country_of_origin → ARTIST
    (302, 302, 1),  -- city_of_origin → ARTIST
    (303, 303, 1),  -- founding_year → ARTIST
    (304, 304, 1);  -- activity_periods → ARTIST
