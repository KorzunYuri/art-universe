-- Update final selection to use input_table, output_table and target_count parameters
DROP FUNCTION IF EXISTS p_quiz_gen_tracks_step_final_selection(TEXT, TEXT, BIGINT, BIGINT, INTEGER, INTEGER);
DROP FUNCTION IF EXISTS p_quiz_gen_tracks_step_final_selection(TEXT, TEXT, INTEGER);

CREATE OR REPLACE FUNCTION p_quiz_gen_tracks_step_final_selection(
    input_table TEXT,
    output_table TEXT,
    target_count INTEGER
) RETURNS VOID AS $$
DECLARE
    input_parts TEXT[];
    output_parts TEXT[];
    actual_input_table TEXT;
BEGIN
    input_parts := p_parse_table_name(input_table);
    output_parts := p_parse_table_name(output_table);

    -- Ensure chance column exists, may return new table name
    actual_input_table := p_ensure_chance_column(input_table);
    input_parts := p_parse_table_name(actual_input_table);

    EXECUTE format('
        CREATE TABLE %I.%I AS
        SELECT track_id, primary_artist_id
        FROM (
            SELECT it.track_id,
                   it.primary_artist_id,
                   ROW_NUMBER() OVER (ORDER BY RANDOM() * COALESCE(it.chance, 1.0) DESC) as rn
            FROM %I.%I it
        ) ranked
        WHERE rn <= %s
    ', output_parts[1], output_parts[2], input_parts[1], input_parts[2], target_count);

    EXECUTE format('CREATE INDEX ON %I.%I (track_id)', output_parts[1], output_parts[2]);
    EXECUTE format('CREATE INDEX ON %I.%I (primary_artist_id)', output_parts[1], output_parts[2]);
END;
$$ LANGUAGE plpgsql;
