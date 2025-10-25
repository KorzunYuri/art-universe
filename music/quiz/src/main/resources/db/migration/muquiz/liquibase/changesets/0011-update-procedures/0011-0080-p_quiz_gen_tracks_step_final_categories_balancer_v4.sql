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
BEGIN
    input_parts := p_parse_table_name(input_table);
    output_parts := p_parse_table_name(output_table);
    quota_parts := p_parse_table_name(quota_table);

    -- Ensure chance column exists, may return new table name
    actual_input_table := p_ensure_chance_column(input_table);
    input_parts := p_parse_table_name(actual_input_table);

    EXECUTE format('
        CREATE TABLE %I.%I AS
        WITH category_quotas AS (
            SELECT tc.track_id,
                   COALESCE(qt.weight, %s) as quota_weight
            FROM mu.track_category tc
            LEFT JOIN %I.%I qt ON tc.category_id = qt.category_id
        ),
        track_quotas AS (
            SELECT it.track_id,
                   it.primary_artist_id,
                   COALESCE(it.chance, 1.0) as base_chance,
                   COALESCE(AVG(cq.quota_weight), %s) as avg_quota
            FROM %I.%I it
            LEFT JOIN category_quotas cq ON it.track_id = cq.track_id
            GROUP BY it.track_id, it.primary_artist_id, it.chance
        )
        SELECT track_id, primary_artist_id
        FROM (
            SELECT tq.track_id,
                   tq.primary_artist_id,
                   ROW_NUMBER() OVER (ORDER BY RANDOM() * tq.base_chance * tq.avg_quota DESC) as rn
            FROM track_quotas tq
        ) ranked
        WHERE rn <= %s
    ', output_parts[1], output_parts[2], default_quota, quota_parts[1], quota_parts[2], 
       default_quota, input_parts[1], input_parts[2], target_count);

    EXECUTE format('CREATE INDEX ON %I.%I (track_id)', output_parts[1], output_parts[2]);
    EXECUTE format('CREATE INDEX ON %I.%I (primary_artist_id)', output_parts[1], output_parts[2]);
END;
$$ LANGUAGE plpgsql;
