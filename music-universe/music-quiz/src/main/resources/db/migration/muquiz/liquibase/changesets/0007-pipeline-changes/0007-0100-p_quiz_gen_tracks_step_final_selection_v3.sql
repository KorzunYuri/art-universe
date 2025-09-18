-- Fix final selection to properly use chance column for artist selection
DROP FUNCTION IF EXISTS p_quiz_gen_tracks_step_final_selection;
CREATE OR REPLACE FUNCTION p_quiz_gen_tracks_step_final_selection(
    input_schema TEXT,
    input_table TEXT,
    game_id BIGINT,
    generation_id BIGINT,
    step_order INTEGER,
    target_count INTEGER
) RETURNS TEXT AS $$
DECLARE
    output_table_name TEXT;
    full_output_table TEXT;
    target_schema TEXT := 'mu_quiz_stg';
    input_table_with_chance TEXT;
BEGIN
    output_table_name := p_quiz_gen_tracks_get_tablename_prefix(game_id, generation_id, step_order) || '_final_out';
    full_output_table := target_schema || '.' || output_table_name;

    -- Ensure input table has chance column
    input_table_with_chance := p_ensure_chance_column(input_schema, input_table);

    -- Create output table with chance-weighted selection and artist deduplication
    EXECUTE format($f$
        CREATE TABLE %I.%I AS
        WITH ranked_tracks AS (
            SELECT
                track_id,
                primary_artist_id,
                ROW_NUMBER() OVER (ORDER BY RANDOM() * chance DESC) AS rank
            FROM %s
        ),
        deduplicated AS (
            SELECT
                track_id,
                primary_artist_id,
                rank,
                ROW_NUMBER() OVER (PARTITION BY primary_artist_id ORDER BY rank) AS artist_rn
            FROM ranked_tracks
        )
        SELECT track_id, primary_artist_id
        FROM deduplicated
        WHERE artist_rn = 1
        ORDER BY rank
        LIMIT %s
    $f$, target_schema, output_table_name, input_table_with_chance, target_count);

    EXECUTE format('CREATE INDEX ON %I.%I (track_id)', target_schema, output_table_name);
    EXECUTE format('CREATE INDEX ON %I.%I (primary_artist_id)', target_schema, output_table_name);

    RETURN full_output_table;
END;
$$ LANGUAGE plpgsql;
