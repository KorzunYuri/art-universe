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
    actual_input_table TEXT;
BEGIN
    input_parts := p_parse_table_name(input_table);
    output_parts := p_parse_table_name(output_table);
    blacklist_parts := p_parse_table_name(blacklist_table);

    -- Ensure chance column exists, may return new table name
    actual_input_table := p_ensure_chance_column(input_table);
    input_parts := p_parse_table_name(actual_input_table);

    EXECUTE format('
        CREATE TABLE %I.%I AS
        SELECT inp.*
        FROM %I.%I inp
        WHERE inp.track_id NOT IN (
            SELECT tc.track_id 
            FROM mu_quiz.mu_v_track_category tc
            JOIN %I.%I bl ON tc.category_id = bl.category_id
        )
    ', output_parts[1], output_parts[2], input_parts[1], input_parts[2], 
       blacklist_parts[1], blacklist_parts[2]);

    EXECUTE format('CREATE INDEX ON %I.%I (track_id)', output_parts[1], output_parts[2]);
    EXECUTE format('CREATE INDEX ON %I.%I (primary_artist_id)', output_parts[1], output_parts[2]);
END;
$$ LANGUAGE plpgsql;
