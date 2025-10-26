-- Update final categories balancer to use input_table, output_table, quota_table and parameters
DROP FUNCTION IF EXISTS p_quiz_gen_tracks_step_final_categories_balancer(TEXT, TEXT, BIGINT, BIGINT, INTEGER, INTEGER, DOUBLE PRECISION, TEXT);
DROP FUNCTION IF EXISTS p_quiz_gen_tracks_step_final_categories_balancer(TEXT, TEXT, TEXT, INTEGER, DOUBLE PRECISION);

CREATE OR REPLACE FUNCTION p_quiz_gen_tracks_step_final_categories_balancer(
    input_table TEXT,
    output_table TEXT,
    quota_table TEXT,
    target_count INTEGER,
    default_quota DOUBLE PRECISION
) RETURNS VOID AS $$
DECLARE
    input_parts TEXT[];
    output_parts TEXT[];
    quota_parts TEXT[];
    actual_input_table TEXT;
    table_prefix TEXT;
BEGIN
    input_parts := p_parse_table_name(input_table);
    output_parts := p_parse_table_name(output_table);
    quota_parts := p_parse_table_name(quota_table);

    -- Ensure chance column exists, may return new table name
    actual_input_table := p_ensure_chance_column(input_table);
    input_parts := p_parse_table_name(actual_input_table);
    
    -- Generate table prefix for intermediate tables
    table_prefix := output_parts[2];

    -- Step 1: Create special_tracks table (tracks with special categories after probabilistic assignment)
    EXECUTE format('DROP TABLE IF EXISTS %I.%I_quota_i1_special_tracks', output_parts[1], table_prefix);
    EXECUTE format('
        CREATE TABLE %I.%I_quota_i1_special_tracks AS
        WITH special_category_mapping AS (
            SELECT 
                inp.track_id,
                inp.primary_artist_id,
                COALESCE(inp.chance, 1.0) as chance,
                qt.category_id as parent_category,
                CEIL(%s * (1.0 - %s) * qt.weight) as quota,
                qt.weight
            FROM %I.%I inp
            JOIN mu_quiz.mu_v_track_category tc ON inp.track_id = tc.track_id
            JOIN %I.%I qt ON tc.category_id = qt.category_id
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
    ', output_parts[1], table_prefix, target_count, default_quota, input_parts[1], input_parts[2],
       quota_parts[1], quota_parts[2], quota_parts[1], quota_parts[2]);

    -- Step 2: Create all_tracks table (special + remaining with default quota)
    EXECUTE format('DROP TABLE IF EXISTS %I.%I_quota_i2_all_tracks', output_parts[1], table_prefix);
    EXECUTE format('
        CREATE TABLE %I.%I_quota_i2_all_tracks AS
        SELECT * FROM %I.%I_quota_i1_special_tracks
        UNION ALL
        SELECT 
            inp.track_id,
            inp.primary_artist_id,
            COALESCE(inp.chance, 1.0) as chance,
            -1 as parent_category,
            CEIL(%s * %s) as quota
        FROM %I.%I inp
        WHERE inp.track_id NOT IN (SELECT track_id FROM %I.%I_quota_i1_special_tracks)
    ', output_parts[1], table_prefix, output_parts[1], table_prefix,
       target_count, default_quota, input_parts[1], input_parts[2], output_parts[1], table_prefix);

    -- Step 3: Apply quotas BEFORE artist deduplication
    EXECUTE format('DROP TABLE IF EXISTS %I.%I_quota_i3_selected', output_parts[1], table_prefix);
    EXECUTE format('
        CREATE TABLE %I.%I_quota_i3_selected AS
        WITH ranked_tracks AS (
            SELECT 
                track_id,
                primary_artist_id,
                chance,
                parent_category,
                quota,
                ROW_NUMBER() OVER (
                    PARTITION BY parent_category 
                    ORDER BY RANDOM() * chance DESC
                ) as rn
            FROM %I.%I_quota_i2_all_tracks
        )
        SELECT 
            track_id,
            primary_artist_id,
            chance,
            parent_category
        FROM ranked_tracks
        WHERE rn <= quota
    ', output_parts[1], table_prefix, output_parts[1], table_prefix);

    -- Final step: Apply artist deduplication AFTER quota selection
    EXECUTE format('
        CREATE TABLE %I.%I AS
        WITH artist_deduplicated AS (
            SELECT 
                track_id,
                primary_artist_id,
                ROW_NUMBER() OVER (
                    PARTITION BY primary_artist_id 
                    ORDER BY RANDOM() * chance DESC
                ) as artist_rn
            FROM %I.%I_quota_i3_selected
        )
        SELECT 
            track_id,
            primary_artist_id
        FROM artist_deduplicated
        WHERE artist_rn = 1
    ', output_parts[1], output_parts[2], output_parts[1], table_prefix);

    EXECUTE format('CREATE INDEX ON %I.%I (track_id)', output_parts[1], output_parts[2]);
    EXECUTE format('CREATE INDEX ON %I.%I (primary_artist_id)', output_parts[1], output_parts[2]);
END;
$$ LANGUAGE plpgsql;
