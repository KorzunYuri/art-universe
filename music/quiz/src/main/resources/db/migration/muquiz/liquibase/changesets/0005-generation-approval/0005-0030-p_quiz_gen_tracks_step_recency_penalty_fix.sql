-- Fix recency penalty procedure - remove dependency on game.generation_id
-- Now looks at all approved generations to calculate recency
CREATE OR REPLACE FUNCTION p_quiz_gen_tracks_step_recency_penalty(
    input_schema TEXT,
    input_table TEXT,
    game_id BIGINT,
    generation_id BIGINT,
    step_id INTEGER
) RETURNS TEXT AS $$
DECLARE
    output_table_name TEXT;
    full_output_table TEXT;
    target_schema TEXT := 'mu_quiz_stg';
    columns_list TEXT;
    has_chance_column BOOLEAN;
BEGIN
    output_table_name := 'gen_tracks_' || lpad(game_id::text, 4, '0') || '_' || lpad(generation_id::text, 4, '0') || '_' || lpad(step_id::text, 2, '0') || '_out';
    full_output_table := target_schema || '.' || output_table_name;
    
    -- Check if input table has chance column
    SELECT EXISTS (
        SELECT 1 FROM information_schema.columns c
        WHERE c.table_schema = input_schema
        AND c.table_name = input_table
        AND c.column_name = 'chance'
    ) INTO has_chance_column;
    
    -- Get list of columns excluding 'chance'
    SELECT string_agg('inp.' || column_name, ', ' ORDER BY ordinal_position)
    INTO columns_list
    FROM information_schema.columns c
    WHERE c.table_schema = input_schema
    AND c.table_name = input_table
    AND c.column_name != 'chance';
    
    -- Create output table with recency penalty applied
    IF has_chance_column THEN
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
            WHERE inp.chance * COALESCE(
                CASE 
                    WHEN lp.months_ago IS NULL THEN 1.0
                    WHEN lp.months_ago >= 12 THEN 1.0
                    WHEN lp.months_ago <= 1 THEN 0.0
                    ELSE (lp.months_ago - 1) / 11.0
                END, 1.0
            ) > 0
        ', target_schema, output_table_name, columns_list, input_schema, input_table);
    ELSE
        EXECUTE format('
            CREATE TABLE %I.%I AS
            SELECT 
                %s,
                COALESCE(
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
            WHERE COALESCE(
                CASE 
                    WHEN lp.months_ago IS NULL THEN 1.0
                    WHEN lp.months_ago >= 12 THEN 1.0
                    WHEN lp.months_ago <= 1 THEN 0.0
                    ELSE (lp.months_ago - 1) / 11.0
                END, 1.0
            ) > 0
        ', target_schema, output_table_name, columns_list, input_schema, input_table);
    END IF;
    
    -- Create indexes for performance
    EXECUTE format('CREATE INDEX ON %I.%I (track_id)', target_schema, output_table_name);
    EXECUTE format('CREATE INDEX ON %I.%I (primary_artist_id)', target_schema, output_table_name);
    
    RETURN full_output_table;
END;
$$ LANGUAGE plpgsql;
