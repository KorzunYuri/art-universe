-- Leaves only approved tracks
-- Extra inputs: none
-- Extra effect: none
CREATE OR REPLACE FUNCTION p_quiz_gen_tracks_step_approved_filter(
    input_schema TEXT,
    input_table TEXT,
    game_id BIGINT,
    generation_id BIGINT,
    step_id INTEGER
) RETURNS TEXT AS $$
DECLARE
    output_table_name TEXT;
    full_output_table TEXT;
    target_schema TEXT := 'mu_quiz_stg';  -- схема, в которой создаются таблицы
BEGIN
    output_table_name := 'gen_tracks_' || lpad(game_id::text, 4, '0') || '_' || lpad(generation_id::text, 4, '0') || '_' || lpad(step_id::text, 2, '0') || '_out';

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
