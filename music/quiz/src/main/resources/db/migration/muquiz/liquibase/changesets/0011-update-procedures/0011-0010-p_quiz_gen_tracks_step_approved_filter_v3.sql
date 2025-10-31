-- Update approved filter to use input_table and output_table parameters
DROP FUNCTION IF EXISTS p_quiz_gen_tracks_step_approved_filter(TEXT, TEXT, BIGINT, BIGINT, INTEGER);

CREATE OR REPLACE FUNCTION p_quiz_gen_tracks_step_approved_filter(
    input_table TEXT,
    output_table TEXT
) RETURNS VOID AS $$
DECLARE
    input_parts TEXT[];
    output_parts TEXT[];
BEGIN
    input_parts := p_parse_table_name(input_table);
    output_parts := p_parse_table_name(output_table);

    EXECUTE format('
        CREATE TABLE %I.%I AS
        SELECT vt.track_id,
               vt.primary_artist_id
        FROM %I.%I vt
        JOIN mu_quiz.track qt ON vt.track_id = qt.master_id
    ', output_parts[1], output_parts[2], input_parts[1], input_parts[2]);

    EXECUTE format('CREATE INDEX ON %I.%I (track_id)', output_parts[1], output_parts[2]);
    EXECUTE format('CREATE INDEX ON %I.%I (primary_artist_id)', output_parts[1], output_parts[2]);
END;
$$ LANGUAGE plpgsql;
