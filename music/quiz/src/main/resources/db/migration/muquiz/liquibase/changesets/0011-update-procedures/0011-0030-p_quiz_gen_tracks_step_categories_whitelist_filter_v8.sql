-- Update whitelist filter to use input_table, output_table and whitelist_table parameters
DROP FUNCTION IF EXISTS p_quiz_gen_tracks_step_categories_whitelist_filter(TEXT, TEXT, BIGINT, BIGINT, INTEGER, TEXT);
DROP FUNCTION IF EXISTS p_quiz_gen_tracks_step_categories_whitelist_filter(TEXT, TEXT, TEXT);

CREATE OR REPLACE FUNCTION p_quiz_gen_tracks_step_categories_whitelist_filter(
    input_table TEXT,
    output_table TEXT,
    whitelist_table TEXT
) RETURNS VOID AS $$
DECLARE
    input_parts TEXT[];
    output_parts TEXT[];
    whitelist_parts TEXT[];
    whitelist_count INTEGER;
BEGIN
    input_parts := p_parse_table_name(input_table);
    output_parts := p_parse_table_name(output_table);
    whitelist_parts := p_parse_table_name(whitelist_table);

    -- Get whitelist count to handle empty whitelist case
    EXECUTE format('SELECT count(*) FROM %I.%I', whitelist_parts[1], whitelist_parts[2]) INTO whitelist_count;

    EXECUTE format('
        CREATE TABLE %I.%I AS
        SELECT it.track_id,
               it.primary_artist_id,
               COALESCE(wl.weight, 1.0) as chance
        FROM %I.%I it
        LEFT JOIN (
            SELECT tc.track_id, MAX(wl.weight) as weight
            FROM mu.track_category tc
            JOIN %I.%I wl ON tc.category_id = wl.category_id
            GROUP BY tc.track_id
        ) wl ON it.track_id = wl.track_id
        WHERE wl.track_id IS NOT NULL OR %s = 0
    ', output_parts[1], output_parts[2], input_parts[1], input_parts[2], 
       whitelist_parts[1], whitelist_parts[2], whitelist_count);

    EXECUTE format('CREATE INDEX ON %I.%I (track_id)', output_parts[1], output_parts[2]);
    EXECUTE format('CREATE INDEX ON %I.%I (primary_artist_id)', output_parts[1], output_parts[2]);
END;
$$ LANGUAGE plpgsql;
