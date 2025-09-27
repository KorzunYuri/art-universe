-- Apply artist diversity penalty - simplified version using p_ensure_chance_column
CREATE OR REPLACE FUNCTION p_quiz_gen_tracks_step_artist_diversity(
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
    input_table_with_chance TEXT;
BEGIN
    output_table_name := 'gen_tracks_' || lpad(game_id::text, 4, '0') || '_' || lpad(generation_id::text, 4, '0') || '_' || lpad(step_id::text, 2, '0') || '_out';
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
    
    -- Create output table with artist diversity penalty applied
    EXECUTE format('
        CREATE TABLE %I.%I AS
        SELECT 
            %s,
            inp.chance * (1.0 / ac.track_count) as chance
        FROM %s inp
        JOIN (
            SELECT 
                primary_artist_id,
                COUNT(*) as track_count
            FROM %s
            GROUP BY primary_artist_id
        ) ac ON inp.primary_artist_id = ac.primary_artist_id
        WHERE inp.chance * (1.0 / ac.track_count) > 0
    ', target_schema, output_table_name, columns_list, input_table_with_chance, input_table_with_chance);
    
    -- Create indexes for performance
    EXECUTE format('CREATE INDEX ON %I.%I (track_id)', target_schema, output_table_name);
    EXECUTE format('CREATE INDEX ON %I.%I (primary_artist_id)', target_schema, output_table_name);
    
    RETURN full_output_table;
END;
$$ LANGUAGE plpgsql;
