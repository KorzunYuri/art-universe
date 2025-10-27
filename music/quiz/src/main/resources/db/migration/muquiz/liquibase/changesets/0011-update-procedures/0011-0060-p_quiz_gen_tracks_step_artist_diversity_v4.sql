-- Update artist diversity to use input_table and output_table parameters
DROP FUNCTION IF EXISTS p_quiz_gen_tracks_step_artist_diversity(TEXT, TEXT, BIGINT, BIGINT, INTEGER);
DROP FUNCTION IF EXISTS p_quiz_gen_tracks_step_artist_diversity(TEXT, TEXT);

CREATE OR REPLACE FUNCTION p_quiz_gen_tracks_step_artist_diversity(
    input_table TEXT,
    output_table TEXT
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
        SELECT 
            inp.track_id,
            inp.primary_artist_id,
            COALESCE(inp.chance, 1.0) * (1.0 / ac.track_count) as chance
        FROM %I.%I inp
        JOIN (
            SELECT 
                primary_artist_id,
                COUNT(*) as track_count
            FROM %I.%I
            GROUP BY primary_artist_id
        ) ac ON inp.primary_artist_id = ac.primary_artist_id
        WHERE COALESCE(inp.chance, 1.0) * (1.0 / ac.track_count) > 0
    ', output_parts[1], output_parts[2], input_parts[1], input_parts[2], input_parts[1], input_parts[2]);

    EXECUTE format('CREATE INDEX ON %I.%I (track_id)', output_parts[1], output_parts[2]);
    EXECUTE format('CREATE INDEX ON %I.%I (primary_artist_id)', output_parts[1], output_parts[2]);
END;
$$ LANGUAGE plpgsql;
