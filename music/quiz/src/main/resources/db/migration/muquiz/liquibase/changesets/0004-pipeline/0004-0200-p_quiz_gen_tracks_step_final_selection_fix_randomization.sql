CREATE OR REPLACE FUNCTION p_quiz_gen_tracks_step_final_selection(
    input_schema TEXT,
    input_table TEXT,
    game_id BIGINT,
    generation_id BIGINT,
    step_id INTEGER,
    target_count INTEGER
) RETURNS TEXT AS $$
DECLARE
    output_table_name TEXT;
    full_output_table TEXT;
    target_schema TEXT := 'mu_quiz_stg';
    has_chance_column BOOLEAN;
BEGIN
    output_table_name := 'gen_tracks_'
                             || lpad(game_id::text, 4, '0') || '_'
                             || lpad(generation_id::text, 4, '0') || '_'
                             || lpad(step_id::text, 2, '0') || '_out';

    full_output_table := target_schema || '.' || output_table_name;

    SELECT EXISTS (
        SELECT 1
        FROM information_schema.columns c
        WHERE c.table_schema = input_schema
          AND c.table_name = input_table
          AND c.column_name = 'chance'
    ) INTO has_chance_column;

    IF has_chance_column THEN
        EXECUTE format($f$
            CREATE TABLE %I.%I AS
            WITH ranked AS (
                SELECT
                    track_id,
                    primary_artist_id,
                    ROW_NUMBER() OVER (
                        PARTITION BY primary_artist_id
                        ORDER BY -LN(RANDOM()) / NULLIF(chance, 0)
                    ) AS rn
                FROM %I.%I
            )
            SELECT track_id, primary_artist_id
            FROM ranked
            WHERE rn = 1
            ORDER BY RANDOM()
            LIMIT %s
        $f$, target_schema, output_table_name, input_schema, input_table, target_count);
    ELSE
        EXECUTE format($f$
            CREATE TABLE %I.%I AS
            WITH ranked AS (
                SELECT
                    track_id,
                    primary_artist_id,
                    ROW_NUMBER() OVER (
                        PARTITION BY primary_artist_id
                        ORDER BY RANDOM()
                    ) AS rn
                FROM %I.%I
            )
            SELECT track_id, primary_artist_id
            FROM ranked
            WHERE rn = 1
            ORDER BY RANDOM()
            LIMIT %s
        $f$, target_schema, output_table_name, input_schema, input_table, target_count);
    END IF;

    EXECUTE format('CREATE INDEX ON %I.%I (track_id)', target_schema, output_table_name);
    EXECUTE format('CREATE INDEX ON %I.%I (primary_artist_id)', target_schema, output_table_name);

    RETURN full_output_table;
END;
$$ LANGUAGE plpgsql;