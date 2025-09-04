-- Update main pipeline to include artist recency penalty step
CREATE OR REPLACE FUNCTION p_quiz_gen_tracks_pipeline(
    game_id BIGINT,
    generation_id BIGINT,
    target_count INTEGER
) RETURNS TEXT AS $$
DECLARE
    table_name_full TEXT;
    schema_name TEXT;
    table_name TEXT;
BEGIN
    -- Step 1: Filter approved tracks and artists
    table_name_full := p_quiz_gen_tracks_step_approved_filter('mu_view', 'v_track', game_id, generation_id, 1);
    
    -- Extract schema and table name for next step
    schema_name := split_part(table_name_full, '.', 1);
    table_name := split_part(table_name_full, '.', 2);
    
    -- Step 2: Apply track recency penalty
    table_name_full := p_quiz_gen_tracks_step_recency_penalty(schema_name, table_name, game_id, generation_id, 2);
    
    -- Extract schema and table name for next step
    schema_name := split_part(table_name_full, '.', 1);
    table_name := split_part(table_name_full, '.', 2);
    
    -- Step 3: Apply artist recency penalty
    table_name_full := p_quiz_gen_tracks_step_artist_recency_penalty(schema_name, table_name, game_id, generation_id, 3);
    
    -- Extract schema and table name for next step
    schema_name := split_part(table_name_full, '.', 1);
    table_name := split_part(table_name_full, '.', 2);
    
    -- Step 4: Apply artist diversity penalty
    table_name_full := p_quiz_gen_tracks_step_artist_diversity(schema_name, table_name, game_id, generation_id, 4);
    
    -- Extract schema and table name for next step
    schema_name := split_part(table_name_full, '.', 1);
    table_name := split_part(table_name_full, '.', 2);
    
    -- Step 5: Final selection
    table_name_full := p_quiz_gen_tracks_step_final_selection(schema_name, table_name, game_id, generation_id, 5, target_count);
    
    RETURN table_name_full;
END;
$$ LANGUAGE plpgsql;
