-- Final categories balancer with explicit default quota
DROP FUNCTION IF EXISTS p_quiz_gen_tracks_step_final_categories_balancer;

CREATE OR REPLACE FUNCTION p_quiz_gen_tracks_step_final_categories_balancer(
    input_schema TEXT,
    input_table TEXT,
    game_id BIGINT,
    generation_id BIGINT,
    step_order INTEGER,
    quota_schema TEXT,
    quota_table TEXT,
    target_count INTEGER,
    default_quota DOUBLE PRECISION
) RETURNS TEXT AS $$
DECLARE
    output_table_name TEXT;
    full_output_table TEXT;
    target_schema TEXT := 'mu_quiz_stg';
    input_table_with_chance TEXT;
    table_prefix TEXT;
BEGIN
    table_prefix := p_quiz_gen_tracks_get_tablename_prefix(game_id, generation_id, step_order);
    output_table_name := table_prefix || '_quota_out';
    full_output_table := target_schema || '.' || output_table_name;
    
    -- Ensure input table has chance column
    input_table_with_chance := p_ensure_chance_column(input_schema, input_table);
    
    -- Step 1: Create special_tracks table (tracks with special categories after probabilistic assignment)
    EXECUTE format('DROP TABLE IF EXISTS %I.%I_quota_i1_special_tracks', target_schema, table_prefix);
    EXECUTE format('
        CREATE TABLE %I.%I_quota_i1_special_tracks AS
        WITH special_category_mapping AS (
            SELECT 
                inp.track_id,
                inp.primary_artist_id,
                inp.chance,
                qt.parent_category,
                CEIL(%s * (1.0 - %s) * qt.weight) as quota,
                qt.weight
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
        special_assignment AS (
            SELECT 
                track_id,
                primary_artist_id,
                chance,
                parent_category,
                quota,
                weight,
                ROW_NUMBER() OVER (PARTITION BY track_id ORDER BY RANDOM() * weight DESC) as category_rn
            FROM special_category_mapping
        )
        SELECT 
            track_id,
            primary_artist_id,
            chance,
            parent_category,
            quota
        FROM special_assignment
        WHERE category_rn = 1
    ', target_schema, table_prefix, target_count, default_quota, input_table_with_chance,
       quota_schema, quota_table, quota_schema, quota_table);

    -- Step 2: Create all_tracks table (special + remaining with default quota)
    EXECUTE format('DROP TABLE IF EXISTS %I.%I_quota_i2_all_tracks', target_schema, table_prefix);
    EXECUTE format('
        CREATE TABLE %I.%I_quota_i2_all_tracks AS
        SELECT * FROM %I.%I_quota_i1_special_tracks
        UNION ALL
        SELECT 
            inp.track_id,
            inp.primary_artist_id,
            inp.chance,
            -1 as parent_category,
            CEIL(%s * %s) as quota
        FROM %s inp
        WHERE inp.track_id NOT IN (SELECT track_id FROM %I.%I_quota_i1_special_tracks)
    ', target_schema, table_prefix, target_schema, table_prefix,
       target_count, default_quota, input_table_with_chance, target_schema, table_prefix);

    -- Step 3: Create artist_deduplicated table
    EXECUTE format('DROP TABLE IF EXISTS %I.%I_quota_i3_artist_dedup', target_schema, table_prefix);
    EXECUTE format('
        CREATE TABLE %I.%I_quota_i3_artist_dedup AS
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
        FROM %I.%I_quota_i2_all_tracks
    ', target_schema, table_prefix, target_schema, table_prefix);

    -- Final step: Create output table with quotas applied
    EXECUTE format('
        CREATE TABLE %I.%I AS
        WITH ranked_tracks AS (
            SELECT 
                track_id,
                primary_artist_id,
                parent_category,
                quota,
                ROW_NUMBER() OVER (
                    PARTITION BY parent_category 
                    ORDER BY RANDOM() * chance DESC
                ) as rn
            FROM %I.%I_quota_i3_artist_dedup
            WHERE artist_rn = 1
        )
        SELECT 
            track_id,
            primary_artist_id
        FROM ranked_tracks
        WHERE rn <= quota
    ', target_schema, output_table_name, target_schema, table_prefix);
    
    -- Create indexes for performance
    EXECUTE format('CREATE INDEX ON %I.%I (track_id)', target_schema, output_table_name);
    EXECUTE format('CREATE INDEX ON %I.%I (primary_artist_id)', target_schema, output_table_name);
    
    RETURN full_output_table;
END;
$$ LANGUAGE plpgsql;
