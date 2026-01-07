-- Filter out tracks by blacklisted artist categories (with hierarchy)
-- Extra inputs: blacklist_schema, blacklist_table (category_id)
-- Extra effect: none
CREATE OR REPLACE FUNCTION p_quiz_gen_tracks_step_categories_blacklist_filter(
    input_schema TEXT,
    input_table TEXT,
    game_id BIGINT,
    generation_id BIGINT,
    step_id INTEGER,
    blacklist_schema TEXT,
    blacklist_table TEXT
) RETURNS TEXT AS $$
DECLARE
    output_table_name TEXT;
    full_output_table TEXT;
    target_schema TEXT := 'mu_quiz_stg';
BEGIN
    output_table_name := 'gen_tracks_' || lpad(game_id::text, 4, '0') || '_' || lpad(generation_id::text, 4, '0') || '_' || lpad(step_id::text, 2, '0') || '_out';
    full_output_table := target_schema || '.' || output_table_name;
    
    -- Create output table excluding blacklisted categories and their children
    EXECUTE format('
        CREATE TABLE %I.%I AS
        SELECT inp.*
        FROM %I.%I inp
        WHERE inp.primary_artist_id NOT IN (
            -- Find artists that have any blacklisted categories (including children)
            SELECT DISTINCT vac.artist_id
            FROM mu_view.v_artist_category vac
            WHERE vac.category_id IN (
                -- Get all child categories of blacklisted ones using pre-computed hierarchy
                SELECT DISTINCT vch.id
                FROM %I.%I bl
                JOIN mu_view.v_category_hierarchy vch ON (
                    vch.id = bl.category_id OR vch.parent_id = bl.category_id
                )
            )
        )
    ', target_schema, output_table_name, input_schema, input_table, blacklist_schema, blacklist_table);
    
    -- Create indexes for performance
    EXECUTE format('CREATE INDEX ON %I.%I (track_id)', target_schema, output_table_name);
    EXECUTE format('CREATE INDEX ON %I.%I (primary_artist_id)', target_schema, output_table_name);
    
    RETURN full_output_table;
END;
$$ LANGUAGE plpgsql;
