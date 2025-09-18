-- Function to generate consistent table name prefix for pipeline steps
DROP FUNCTION IF EXISTS p_quiz_gen_tracks_get_tablename_prefix;
CREATE OR REPLACE FUNCTION p_quiz_gen_tracks_get_tablename_prefix(
    game_id BIGINT,
    generation_id BIGINT,
    step_order INTEGER
) RETURNS TEXT AS $$
BEGIN
    RETURN 'gen_tracks_' || lpad(game_id::text, 4, '0') || '_' || lpad(generation_id::text, 4, '0') || '_' || lpad(step_order::text, 2, '0');
END;
$$ LANGUAGE plpgsql;
