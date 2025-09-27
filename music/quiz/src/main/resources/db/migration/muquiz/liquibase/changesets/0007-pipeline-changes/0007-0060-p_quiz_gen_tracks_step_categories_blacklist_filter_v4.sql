-- Update blacklist filter to use new table naming convention
DROP FUNCTION IF EXISTS p_quiz_gen_tracks_step_categories_blacklist_filter;
CREATE OR REPLACE FUNCTION p_quiz_gen_tracks_step_categories_blacklist_filter(
    input_schema TEXT,
    input_table TEXT,
    game_id BIGINT,
    generation_id BIGINT,
    step_order INTEGER,
    blacklist_schema TEXT,
    blacklist_table TEXT
) RETURNS TEXT AS $$
DECLARE
    output_table_name TEXT;
    full_output_table TEXT;
    target_schema TEXT := 'mu_quiz_stg';
    input_table_with_chance TEXT;
BEGIN
    output_table_name := p_quiz_gen_tracks_get_tablename_prefix(game_id, generation_id, step_order) || '_bl_out';
    full_output_table := target_schema || '.' || output_table_name;
    
    -- Ensure input table has chance column
    input_table_with_chance := p_ensure_chance_column(input_schema, input_table);
    
    -- Create output table excluding blacklisted categories and their children
    EXECUTE format('
        CREATE TABLE %I.%I AS
        SELECT inp.*
        FROM %s inp
        WHERE inp.primary_artist_id NOT IN (
            -- Find artists that have any blacklisted categories (including children)
            SELECT DISTINCT vac.artist_id
            FROM mu_view.v_artist_category vac
            WHERE vac.category_id IN (
                -- Get all child categories of blacklisted ones using new view
                SELECT DISTINCT vcc.child_id
                FROM %I.%I bl
                JOIN mu_view.v_category_children vcc ON vcc.id = bl.category_id
                UNION
                SELECT DISTINCT bl.category_id
                FROM %I.%I bl
            )
        )
    ', target_schema, output_table_name, input_table_with_chance, blacklist_schema, blacklist_table, blacklist_schema, blacklist_table);
    
    -- Create indexes for performance
    EXECUTE format('CREATE INDEX ON %I.%I (track_id)', target_schema, output_table_name);
    EXECUTE format('CREATE INDEX ON %I.%I (primary_artist_id)', target_schema, output_table_name);
    
    RETURN full_output_table;
END;
$$ LANGUAGE plpgsql;
