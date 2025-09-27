-- Update approved filter to use new table naming convention
DROP FUNCTION IF EXISTS p_quiz_gen_tracks_step_approved_filter;
CREATE OR REPLACE FUNCTION p_quiz_gen_tracks_step_approved_filter(
    input_schema TEXT,
    input_table TEXT,
    game_id BIGINT,
    generation_id BIGINT,
    step_order INTEGER
) RETURNS TEXT AS $$
DECLARE
    output_table_name TEXT;
    full_output_table TEXT;
    target_schema TEXT := 'mu_quiz_stg';
BEGIN
    output_table_name := p_quiz_gen_tracks_get_tablename_prefix(game_id, generation_id, step_order) || '_approved_out';
    full_output_table := target_schema || '.' || output_table_name;

    EXECUTE format('
        CREATE TABLE %I.%I AS
        SELECT vt.id AS track_id,
               vt.primary_artist_id
        FROM %I.%I vt
        JOIN mu_quiz.track qt ON vt.id = qt.master_id
    ', target_schema, output_table_name, input_schema, input_table);

    EXECUTE format('CREATE INDEX ON %I.%I (track_id)', target_schema, output_table_name);
    EXECUTE format('CREATE INDEX ON %I.%I (primary_artist_id)', target_schema, output_table_name);

    RETURN full_output_table;
END;
$$ LANGUAGE plpgsql;
