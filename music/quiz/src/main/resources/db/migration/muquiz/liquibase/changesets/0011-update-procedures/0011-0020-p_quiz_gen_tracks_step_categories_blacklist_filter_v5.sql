-- Update blacklist filter to use input_table, output_table and blacklist_table parameters
DROP FUNCTION IF EXISTS p_quiz_gen_tracks_step_categories_blacklist_filter(TEXT, TEXT, BIGINT, BIGINT, INTEGER, TEXT);
DROP FUNCTION IF EXISTS p_quiz_gen_tracks_step_categories_blacklist_filter(TEXT, TEXT, TEXT);

CREATE OR REPLACE FUNCTION p_quiz_gen_tracks_step_categories_blacklist_filter(
    input_table TEXT,
    output_table TEXT,
    blacklist_table TEXT
) RETURNS VOID AS $$
DECLARE
    input_parts TEXT[];
    output_parts TEXT[];
    blacklist_parts TEXT[];
BEGIN
    input_parts := p_parse_table_name(input_table);
    output_parts := p_parse_table_name(output_table);
    blacklist_parts := p_parse_table_name(blacklist_table);

    EXECUTE format('
        CREATE TABLE %I.%I AS
        SELECT it.track_id,
               it.primary_artist_id
        FROM %I.%I it
        WHERE NOT EXISTS (
            SELECT 1 
            FROM mu.track_category tc
            JOIN %I.%I bl ON tc.category_id = bl.category_id
            WHERE tc.track_id = it.track_id
        )
    ', output_parts[1], output_parts[2], input_parts[1], input_parts[2], 
       blacklist_parts[1], blacklist_parts[2]);

    EXECUTE format('CREATE INDEX ON %I.%I (track_id)', output_parts[1], output_parts[2]);
    EXECUTE format('CREATE INDEX ON %I.%I (primary_artist_id)', output_parts[1], output_parts[2]);
END;
$$ LANGUAGE plpgsql;
