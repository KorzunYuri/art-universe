-- Update final selection to use input_table, output_table and target_count parameters
DROP FUNCTION IF EXISTS p_quiz_gen_tracks_step_final_selection(TEXT, TEXT, BIGINT, BIGINT, INTEGER, INTEGER);

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
        WITH ranked_tracks AS (
            SELECT
                track_id,
                primary_artist_id,
                ROW_NUMBER() OVER (ORDER BY RANDOM() * COALESCE(chance, 1.0) DESC) AS rank
            FROM %I.%I
        ),
        deduplicated AS (
            SELECT
                track_id,
                primary_artist_id,
                rank,
                ROW_NUMBER() OVER (PARTITION BY primary_artist_id ORDER BY rank) AS artist_rn
            FROM ranked_tracks
        )
        SELECT track_id, primary_artist_id
        FROM deduplicated
        WHERE artist_rn = 1
        ORDER BY rank
        LIMIT %s
    ', output_parts[1], output_parts[2], input_parts[1], input_parts[2], target_count);

    EXECUTE format('CREATE INDEX ON %I.%I (track_id)', output_parts[1], output_parts[2]);
    EXECUTE format('CREATE INDEX ON %I.%I (primary_artist_id)', output_parts[1], output_parts[2]);
END;
$$ LANGUAGE plpgsql;
