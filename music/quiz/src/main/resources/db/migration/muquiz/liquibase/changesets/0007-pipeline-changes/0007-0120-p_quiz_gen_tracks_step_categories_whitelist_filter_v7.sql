-- Optimized whitelist filter with balance compensation
DROP FUNCTION IF EXISTS p_quiz_gen_tracks_step_categories_whitelist_filter;
CREATE OR REPLACE FUNCTION p_quiz_gen_tracks_step_categories_whitelist_filter(
    input_schema TEXT,
    input_table TEXT,
    game_id BIGINT,
    generation_id BIGINT,
    step_order INTEGER,
    whitelist_schema TEXT,
    whitelist_table TEXT
) RETURNS TEXT AS $$
DECLARE
    output_table_name TEXT;
    full_output_table TEXT;
    target_schema TEXT := 'mu_quiz_stg';
    columns_list TEXT;
    input_table_with_chance TEXT;
BEGIN
    output_table_name := p_quiz_gen_tracks_get_tablename_prefix(game_id, generation_id, step_order) || '_wl_out';
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
    
    -- Create output table with compensation-based balancing
    EXECUTE format('
        CREATE TABLE %I.%I AS
        WITH category_mapping AS (
            SELECT 
                inp.track_id,
                inp.primary_artist_id,
                inp.chance,
                vac.category_id,
                wl.weight as target_weight
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
        ),
        category_stats AS (
            SELECT 
                category_id,
                target_weight,
                SUM(chance) as current_chance_sum
            FROM category_mapping
            GROUP BY category_id, target_weight
        ),
        total_stats AS (
            SELECT SUM(current_chance_sum) as total_current_chance
            FROM category_stats
        ),
        compensation_factors AS (
            SELECT 
                cs.category_id,
                CASE 
                    WHEN cs.current_chance_sum > 0 
                    THEN (cs.target_weight * ts.total_current_chance) / cs.current_chance_sum
                    ELSE 1.0 
                END as compensation_factor
            FROM category_stats cs
            CROSS JOIN total_stats ts
        )
        SELECT 
            %s,
            MAX(cm.chance * cf.compensation_factor) as chance
        FROM %s inp
        JOIN category_mapping cm ON inp.track_id = cm.track_id
        JOIN compensation_factors cf ON cm.category_id = cf.category_id
        GROUP BY %s
        HAVING MAX(cm.chance * cf.compensation_factor) > 0
    ', target_schema, output_table_name, input_table_with_chance,
       whitelist_schema, whitelist_table, whitelist_schema, whitelist_table,
       columns_list, input_table_with_chance, columns_list);
    
    -- Create indexes for performance
    EXECUTE format('CREATE INDEX ON %I.%I (track_id)', target_schema, output_table_name);
    EXECUTE format('CREATE INDEX ON %I.%I (primary_artist_id)', target_schema, output_table_name);
    
    RETURN full_output_table;
END;
$$ LANGUAGE plpgsql;
