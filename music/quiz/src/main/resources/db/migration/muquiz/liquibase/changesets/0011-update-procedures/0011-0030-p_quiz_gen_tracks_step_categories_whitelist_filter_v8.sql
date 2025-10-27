-- Update whitelist filter to use input_table, output_table and whitelist_table parameters with v7 logic
DROP FUNCTION IF EXISTS p_quiz_gen_tracks_step_categories_whitelist_filter(TEXT, TEXT, BIGINT, BIGINT, INTEGER, TEXT);
DROP FUNCTION IF EXISTS p_quiz_gen_tracks_step_categories_whitelist_filter(TEXT, TEXT, TEXT);

CREATE OR REPLACE FUNCTION p_quiz_gen_tracks_step_categories_whitelist_filter(
    input_table TEXT,
    output_table TEXT,
    whitelist_table TEXT
) RETURNS VOID AS $$
DECLARE
    input_parts TEXT[];
    output_parts TEXT[];
    whitelist_parts TEXT[];
    actual_input_table TEXT;
BEGIN
    input_parts := p_parse_table_name(input_table);
    output_parts := p_parse_table_name(output_table);
    whitelist_parts := p_parse_table_name(whitelist_table);

    -- Ensure chance column exists, may return new table name
    actual_input_table := p_ensure_chance_column(input_table);
    input_parts := p_parse_table_name(actual_input_table);

    -- Create output table with compensation-based balancing using new view
    EXECUTE format('
        CREATE TABLE %I.%I AS
        WITH category_mapping AS (
            SELECT 
                inp.track_id,
                inp.primary_artist_id,
                COALESCE(inp.chance, 1.0) as chance,
                tc.category_id,
                wl.weight as target_weight
            FROM %I.%I inp
            JOIN mu_quiz.mu_v_track_category tc ON inp.track_id = tc.track_id
            JOIN %I.%I wl ON tc.category_id = wl.category_id
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
            inp.track_id,
            inp.primary_artist_id,
            MAX(cm.chance * cf.compensation_factor) as chance
        FROM %I.%I inp
        JOIN category_mapping cm ON inp.track_id = cm.track_id
        JOIN compensation_factors cf ON cm.category_id = cf.category_id
        GROUP BY inp.track_id, inp.primary_artist_id
        HAVING MAX(cm.chance * cf.compensation_factor) > 0
    ', output_parts[1], output_parts[2], input_parts[1], input_parts[2], 
       whitelist_parts[1], whitelist_parts[2], input_parts[1], input_parts[2]);

    EXECUTE format('CREATE INDEX ON %I.%I (track_id)', output_parts[1], output_parts[2]);
    EXECUTE format('CREATE INDEX ON %I.%I (primary_artist_id)', output_parts[1], output_parts[2]);
END;
$$ LANGUAGE plpgsql;
