-- Update track recency penalty to use input_table and output_table parameters
DROP FUNCTION IF EXISTS p_quiz_gen_tracks_step_track_recency_penalty(TEXT, TEXT, BIGINT, BIGINT, INTEGER);

CREATE OR REPLACE FUNCTION p_quiz_gen_tracks_step_track_recency_penalty(
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
            COALESCE(inp.chance, 1.0) * COALESCE(
                CASE 
                    WHEN lp.months_ago IS NULL THEN 1.0
                    WHEN lp.months_ago >= 12 THEN 1.0
                    WHEN lp.months_ago <= 1 THEN 0.0
                    ELSE (lp.months_ago - 1) / 11.0
                END, 1.0
            ) as chance
        FROM %I.%I inp
        LEFT JOIN (
            SELECT 
                gt.track_id,
                EXTRACT(EPOCH FROM (NOW() - MAX(gen.created_at))) / (30 * 24 * 3600) as months_ago
            FROM mu_quiz.generation_track gt
            JOIN mu_quiz.generation gen ON gt.generation_id = gen.id
            WHERE gen.approved = true
            GROUP BY gt.track_id
        ) lp ON inp.track_id = lp.track_id
        WHERE COALESCE(inp.chance, 1.0) * COALESCE(
            CASE 
                WHEN lp.months_ago IS NULL THEN 1.0
                WHEN lp.months_ago >= 12 THEN 1.0
                WHEN lp.months_ago <= 1 THEN 0.0
                ELSE (lp.months_ago - 1) / 11.0
            END, 1.0
        ) > 0
    ', output_parts[1], output_parts[2], input_parts[1], input_parts[2]);

    EXECUTE format('CREATE INDEX ON %I.%I (track_id)', output_parts[1], output_parts[2]);
    EXECUTE format('CREATE INDEX ON %I.%I (primary_artist_id)', output_parts[1], output_parts[2]);
END;
$$ LANGUAGE plpgsql;
