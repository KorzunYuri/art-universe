-- Migrate existing games and generations to new pipeline structure

-- Create basic pipelines for games without pipeline_id
DO $$
DECLARE
    game_rec RECORD;
    new_pipeline_id BIGINT;
    new_start_step_id BIGINT;
    new_final_step_id BIGINT;
BEGIN
    FOR game_rec IN SELECT id FROM mu_quiz.game WHERE pipeline_id IS NULL
    LOOP
        -- Create basic pipeline for game
        INSERT INTO mu_quiz.pipeline (immutable, created_at, updated_at)
        VALUES (FALSE, NOW(), NOW())
        RETURNING id INTO new_pipeline_id;
        
        -- Create START_DATASOURCE step (type=9, version=1)
        INSERT INTO mu_quiz.step (type, alg_version, cfg_data, deleted, immutable, created_at, updated_at)
        VALUES (9, 1, '{"datasource": "mu_view.v_track"}'::jsonb, FALSE, FALSE, NOW(), NOW())
        RETURNING id INTO new_start_step_id;
        
        -- Create FINAL_LIMITER step (type=7, version=1)
        INSERT INTO mu_quiz.step (type, alg_version, cfg_data, deleted, immutable, created_at, updated_at)
        VALUES (7, 1, '{"targetCount": 50}'::jsonb, FALSE, FALSE, NOW(), NOW())
        RETURNING id INTO new_final_step_id;
        
        -- Create pipeline steps
        INSERT INTO mu_quiz.pipeline_step (pipeline_id, step_id, ord, created_at, updated_at)
        VALUES 
            (new_pipeline_id, new_start_step_id, 0, NOW(), NOW()),
            (new_pipeline_id, new_final_step_id, 1, NOW(), NOW());
        
        -- Update game with pipeline_id
        UPDATE mu_quiz.game SET pipeline_id = new_pipeline_id WHERE id = game_rec.id;
    END LOOP;
END $$;

-- Create immutable pipelines for generations
DO $$
DECLARE
    gen_rec RECORD;
    new_pipeline_id BIGINT;
    new_start_step_id BIGINT;
    new_final_step_id BIGINT;
    new_pipeline_run_id BIGINT;
    new_start_step_run_id BIGINT;
    new_final_step_run_id BIGINT;
BEGIN
    FOR gen_rec IN SELECT id, game_id, target_count, result_table_name, created_at FROM mu_quiz.generation WHERE pipeline_id IS NULL
    LOOP
        -- Create immutable pipeline for generation
        INSERT INTO mu_quiz.pipeline (immutable, created_at, updated_at)
        VALUES (TRUE, gen_rec.created_at, gen_rec.created_at)
        RETURNING id INTO new_pipeline_id;
        
        -- Create immutable START_DATASOURCE step
        INSERT INTO mu_quiz.step (type, alg_version, cfg_data, deleted, immutable, created_at, updated_at)
        VALUES (9, 1, '{"datasource": "mu_view.v_track"}'::jsonb, FALSE, TRUE, gen_rec.created_at, gen_rec.created_at)
        RETURNING id INTO new_start_step_id;
        
        -- Create immutable FINAL_LIMITER step with generation's target_count
        INSERT INTO mu_quiz.step (type, alg_version, cfg_data, deleted, immutable, created_at, updated_at)
        VALUES (7, 1, ('{"targetCount": ' || gen_rec.target_count || '}')::jsonb, FALSE, TRUE, gen_rec.created_at, gen_rec.created_at)
        RETURNING id INTO new_final_step_id;
        
        -- Create pipeline steps
        INSERT INTO mu_quiz.pipeline_step (pipeline_id, step_id, ord, created_at, updated_at)
        VALUES 
            (new_pipeline_id, new_start_step_id, 0, gen_rec.created_at, gen_rec.created_at),
            (new_pipeline_id, new_final_step_id, 1, gen_rec.created_at, gen_rec.created_at);
        
        -- Create pipeline run
        INSERT INTO mu_quiz.pipeline_run (pipeline_id, status, started_at, completed_at, result_table_name, created_at, updated_at)
        VALUES (new_pipeline_id, 3, gen_rec.created_at, gen_rec.created_at, gen_rec.result_table_name, gen_rec.created_at, gen_rec.created_at)
        RETURNING id INTO new_pipeline_run_id;
        
        -- Create step runs
        INSERT INTO mu_quiz.step_run (pipeline_run_id, step_id, step_type, alg_version, step_cfg_data, status, started_at, completed_at, input_table_name, result_table_name, result_stats, created_at, updated_at)
        VALUES (new_pipeline_run_id, new_start_step_id, 9, 1, '{"datasource": "mu_view.v_track"}'::jsonb, 3, gen_rec.created_at, gen_rec.created_at, NULL, 'mu_view.v_track', '{"inputRecords": 0, "filteredRecords": 0, "outputRecords": 0, "inputArtists": 0, "filteredArtists": 0, "outputArtists": 0, "executionTimeMs": 0}'::jsonb, gen_rec.created_at, gen_rec.created_at)
        RETURNING id INTO new_start_step_run_id;
        
        INSERT INTO mu_quiz.step_run (pipeline_run_id, step_id, step_type, alg_version, step_cfg_data, status, started_at, completed_at, input_table_name, result_table_name, result_stats, created_at, updated_at)
        VALUES (new_pipeline_run_id, new_final_step_id, 7, 1, ('{"targetCount": ' || gen_rec.target_count || '}')::jsonb, 3, gen_rec.created_at, gen_rec.created_at, 'mu_view.v_track', gen_rec.result_table_name, '{"inputRecords": 0, "filteredRecords": 0, "outputRecords": 0, "inputArtists": 0, "filteredArtists": 0, "outputArtists": 0, "executionTimeMs": 0}'::jsonb, gen_rec.created_at, gen_rec.created_at)
        RETURNING id INTO new_final_step_run_id;
        
        -- Update step last_step_run_id references
        UPDATE mu_quiz.step SET last_step_run_id = new_start_step_run_id WHERE id = new_start_step_id;
        UPDATE mu_quiz.step SET last_step_run_id = new_final_step_run_id WHERE id = new_final_step_id;
        
        -- Update generation with pipeline_id and pipeline_run_id
        UPDATE mu_quiz.generation 
        SET pipeline_id = new_pipeline_id, pipeline_run_id = new_pipeline_run_id 
        WHERE id = gen_rec.id;
    END LOOP;
END $$;

-- Update sequences to match current data
SELECT setval('mu_quiz.pipeline_seq', (SELECT COALESCE(MAX(id), 1) FROM mu_quiz.pipeline));
SELECT setval('mu_quiz.step_seq', (SELECT COALESCE(MAX(id), 1) FROM mu_quiz.step));
SELECT setval('mu_quiz.pipeline_run_seq', (SELECT COALESCE(MAX(id), 1) FROM mu_quiz.pipeline_run));
SELECT setval('mu_quiz.step_run_seq', (SELECT COALESCE(MAX(id), 1) FROM mu_quiz.step_run));
