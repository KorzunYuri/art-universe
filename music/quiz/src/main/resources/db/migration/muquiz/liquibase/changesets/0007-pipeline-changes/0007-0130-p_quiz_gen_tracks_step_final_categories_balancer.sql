-- Final categories balancer with strict quotas
DROP FUNCTION IF EXISTS p_quiz_gen_tracks_step_final_categories_balancer;
CREATE OR REPLACE FUNCTION p_quiz_gen_tracks_step_final_categories_balancer(
    input_schema TEXT,
    input_table TEXT,
    game_id BIGINT,
    generation_id BIGINT,
    step_order INTEGER,
    quota_schema TEXT,
    quota_table TEXT,
    target_count INTEGER
) RETURNS TEXT AS $$
DECLARE
    output_table_name TEXT;
    full_output_table TEXT;
    target_schema TEXT := 'mu_quiz_stg';
    input_table_with_chance TEXT;
BEGIN
    output_table_name := p_quiz_gen_tracks_get_tablename_prefix(game_id, generation_id, step_order) || '_quota_out';
    full_output_table := target_schema || '.' || output_table_name;
    
    -- Ensure input table has chance column
    input_table_with_chance := p_ensure_chance_column(input_schema, input_table);
    
    -- Create output table with probabilistic category assignment and artist deduplication
    EXECUTE format('
        CREATE TABLE %I.%I AS
        WITH category_mapping AS (
            SELECT 
                inp.track_id,
                inp.primary_artist_id,
                inp.chance,
                qt.parent_category,
                qt.weight,
                CEIL(%s * qt.weight) as quota
            FROM %s inp
            JOIN mu_view.v_artist_category vac ON inp.primary_artist_id = vac.artist_id
            JOIN (
                SELECT DISTINCT vcc.child_id as category_id, qt.category_id as parent_category, qt.weight
                FROM %I.%I qt
                JOIN mu_view.v_category_children vcc ON vcc.id = qt.category_id
                UNION
                SELECT DISTINCT qt.category_id, qt.category_id as parent_category, qt.weight
                FROM %I.%I qt
            ) qt ON vac.category_id = qt.category_id
        ),
        -- Probabilistic assignment of tracks to categories based on weights
        probabilistic_assignment AS (
            SELECT 
                track_id,
                primary_artist_id,
                chance,
                parent_category,
                quota,
                weight,
                ROW_NUMBER() OVER (PARTITION BY track_id ORDER BY RANDOM() * weight DESC) as category_rn
            FROM category_mapping
        ),
        -- Take one category per track (probabilistically)
        single_category_per_track AS (
            SELECT 
                track_id,
                primary_artist_id,
                chance,
                parent_category,
                quota
            FROM probabilistic_assignment
            WHERE category_rn = 1
        ),
        -- Deduplicate by artist within each category
        artist_deduplicated_per_category AS (
            SELECT 
                track_id,
                primary_artist_id,
                chance,
                parent_category,
                quota,
                ROW_NUMBER() OVER (
                    PARTITION BY parent_category, primary_artist_id 
                    ORDER BY RANDOM() * chance DESC
                ) as artist_rn
            FROM single_category_per_track
        ),
        -- Apply quotas within each category
        ranked_tracks AS (
            SELECT 
                track_id,
                primary_artist_id,
                parent_category,
                quota,
                ROW_NUMBER() OVER (
                    PARTITION BY parent_category 
                    ORDER BY RANDOM() * chance DESC
                ) as rn
            FROM artist_deduplicated_per_category
            WHERE artist_rn = 1  -- One track per artist per category
        )
        SELECT 
            track_id,
            primary_artist_id
        FROM ranked_tracks
        WHERE rn <= quota
    ', target_schema, output_table_name, target_count, input_table_with_chance,
       quota_schema, quota_table, quota_schema, quota_table);
    
    -- Create indexes for performance
    EXECUTE format('CREATE INDEX ON %I.%I (track_id)', target_schema, output_table_name);
    EXECUTE format('CREATE INDEX ON %I.%I (primary_artist_id)', target_schema, output_table_name);
    
    RETURN full_output_table;
END;
$$ LANGUAGE plpgsql;
