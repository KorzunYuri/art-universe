-- Final selection with deduplication and random sampling
-- Deduplicates by track_id and primary_artist_id, selects randomly by chance
-- Extra inputs: target_count (target number of records)
-- Extra effect: none
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
    output_table_name := 'gen_tracks_' || lpad(game_id::text, 4, '0') || '_' || lpad(generation_id::text, 4, '0') || '_' || lpad(step_id::text, 2, '0') || '_out';
    full_output_table := target_schema || '.' || output_table_name;
    
    -- Check if input table has chance column
    SELECT EXISTS (
        SELECT 1 FROM information_schema.columns c
        WHERE c.table_schema = input_schema
        AND c.table_name = input_table
        AND c.column_name = 'chance'
    ) INTO has_chance_column;
    
    -- Create output table with final selection
    IF has_chance_column THEN
        EXECUTE format('
            CREATE TABLE %I.%I AS
            SELECT DISTINCT ON (inp.primary_artist_id)
                inp.track_id,
                inp.primary_artist_id
            FROM (
                SELECT DISTINCT ON (track_id)
                    track_id,
                    primary_artist_id,
                    chance
                FROM %I.%I
                ORDER BY track_id, RANDOM() * chance DESC
            ) inp
            ORDER BY inp.primary_artist_id, RANDOM() * inp.chance DESC
            LIMIT %s
        ', target_schema, output_table_name, input_schema, input_table, target_count);
    ELSE
        EXECUTE format('
            CREATE TABLE %I.%I AS
            SELECT DISTINCT ON (inp.primary_artist_id)
                inp.track_id,
                inp.primary_artist_id
            FROM (
                SELECT DISTINCT ON (track_id)
                    track_id,
                    primary_artist_id
                FROM %I.%I
                ORDER BY track_id, RANDOM()
            ) inp
            ORDER BY inp.primary_artist_id, RANDOM()
            LIMIT %s
        ', target_schema, output_table_name, input_schema, input_table, target_count);
    END IF;
    
    -- Create indexes for performance
    EXECUTE format('CREATE INDEX ON %I.%I (track_id)', target_schema, output_table_name);
    EXECUTE format('CREATE INDEX ON %I.%I (primary_artist_id)', target_schema, output_table_name);
    
    RETURN full_output_table;
END;
$$ LANGUAGE plpgsql;
