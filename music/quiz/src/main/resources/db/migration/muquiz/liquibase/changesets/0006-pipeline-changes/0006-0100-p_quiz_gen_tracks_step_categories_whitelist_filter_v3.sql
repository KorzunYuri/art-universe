-- Fix whitelist filter to use new v_category_children view
CREATE OR REPLACE FUNCTION p_quiz_gen_tracks_step_categories_whitelist_filter(
    input_schema TEXT,
    input_table TEXT,
    game_id BIGINT,
    generation_id BIGINT,
    step_id INTEGER,
    whitelist_schema TEXT,
    whitelist_table TEXT
) RETURNS TEXT AS $$
DECLARE
    output_table_name TEXT;
    full_output_table TEXT;
    target_schema TEXT := 'mu_quiz_stg';
    columns_list TEXT;
    group_by_list TEXT;
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
    
    -- Get GROUP BY list (same as columns_list but without 'inp.' prefix)
    SELECT string_agg(column_name, ', ' ORDER BY ordinal_position)
    INTO group_by_list
    FROM information_schema.columns c
    WHERE c.table_schema = split_part(input_table_with_chance, '.', 1)
    AND c.table_name = split_part(input_table_with_chance, '.', 2)
    AND c.column_name != 'chance';
    
    -- Create output table with whitelisted categories and MAX weight calculation
    EXECUTE format('
        CREATE TABLE %I.%I AS
        SELECT 
            %s,
            inp.chance * MAX(wl.weight) as chance
        FROM %s inp
        JOIN mu_view.v_artist_category vac ON inp.primary_artist_id = vac.artist_id
        JOIN (
            SELECT DISTINCT vcc.child_id as category_id, wl.weight
            FROM %I.%I wl
            JOIN mu_view.v_category_children vcc ON vcc.id = wl.category_id
            UNION
            SELECT DISTINCT wl.category_id, wl.weight
            FROM %I.%I wl
        ) wl ON vac.category_id = wl.category_id
        GROUP BY %s, inp.chance
        HAVING inp.chance * MAX(wl.weight) > 0
    ', target_schema, output_table_name, columns_list, input_table_with_chance, 
       whitelist_schema, whitelist_table, whitelist_schema, whitelist_table, group_by_list);
    
    -- Create indexes for performance
    EXECUTE format('CREATE INDEX ON %I.%I (track_id)', target_schema, output_table_name);
    EXECUTE format('CREATE INDEX ON %I.%I (primary_artist_id)', target_schema, output_table_name);
    
    RETURN full_output_table;
END;
$$ LANGUAGE plpgsql;
