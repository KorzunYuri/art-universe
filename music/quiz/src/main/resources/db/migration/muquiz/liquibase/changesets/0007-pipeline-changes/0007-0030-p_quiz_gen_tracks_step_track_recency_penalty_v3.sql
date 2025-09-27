DROP FUNCTION IF EXISTS p_quiz_gen_tracks_step_recency_penalty;
DROP FUNCTION IF EXISTS p_quiz_gen_tracks_step_track_recency_penalty;

-- Update track recency penalty to use new table naming convention
CREATE OR REPLACE FUNCTION p_quiz_gen_tracks_step_track_recency_penalty(
    input_schema TEXT,
    input_table TEXT,
    game_id BIGINT,
    generation_id BIGINT,
    step_order INTEGER
) RETURNS TEXT AS $$
DECLARE
    output_table_name TEXT;
    full_output_table TEXT;
    target_schema TEXT := 'mu_quiz_stg';
    columns_list TEXT;
    input_table_with_chance TEXT;
BEGIN
    output_table_name := p_quiz_gen_tracks_get_tablename_prefix(game_id, generation_id, step_order) || '_track_recency_out';
    full_output_table := target_schema || '.' || output_table_name;
    
    -- Ensure input table has chance column
    input_table_with_chance := p_ensure_chance_column(input_schema, input_table);
    
    -- Get list of columns excluding 'chance'
    SELECT string_agg('inp.' || column_name, ', ' ORDER BY ordinal_position)
    INTO columns_list
    FROM information_schema.columns c
    WHERE c.table_schema = split_part(input_table_with_chance, '.', 1)
    AND c.table_name = split_part(input_table_with_chance, '.', 2)
    AND c.column_name != 'chance';
    
    -- Create output table with recency penalty applied
    EXECUTE format('
        CREATE TABLE %I.%I AS
        SELECT 
            %s,
            inp.chance * COALESCE(
                CASE 
                    WHEN lp.months_ago IS NULL THEN 1.0
                    WHEN lp.months_ago >= 12 THEN 1.0
                    WHEN lp.months_ago <= 1 THEN 0.0
                    ELSE (lp.months_ago - 1) / 11.0
                END, 1.0
            ) as chance
        FROM %s inp
        LEFT JOIN (
            SELECT 
                gt.track_id,
                EXTRACT(EPOCH FROM (NOW() - MAX(gen.created_at))) / (30 * 24 * 3600) as months_ago
            FROM mu_quiz.generation_track gt
            JOIN mu_quiz.generation gen ON gt.generation_id = gen.id
            WHERE gen.approved = true
            GROUP BY gt.track_id
        ) lp ON inp.track_id = lp.track_id
        WHERE inp.chance * COALESCE(
            CASE 
                WHEN lp.months_ago IS NULL THEN 1.0
                WHEN lp.months_ago >= 12 THEN 1.0
                WHEN lp.months_ago <= 1 THEN 0.0
                ELSE (lp.months_ago - 1) / 11.0
            END, 1.0
        ) > 0
    ', target_schema, output_table_name, columns_list, input_table_with_chance);
    
    -- Create indexes for performance
    EXECUTE format('CREATE INDEX ON %I.%I (track_id)', target_schema, output_table_name);
    EXECUTE format('CREATE INDEX ON %I.%I (primary_artist_id)', target_schema, output_table_name);
    
    RETURN full_output_table;
END;
$$ LANGUAGE plpgsql;