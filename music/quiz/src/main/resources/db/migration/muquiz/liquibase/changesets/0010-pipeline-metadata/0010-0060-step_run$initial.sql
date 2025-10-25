-- Create step_run table
CREATE TABLE mu_quiz.step_run (
    id BIGSERIAL PRIMARY KEY,
    pipeline_run_id BIGINT REFERENCES mu_quiz.pipeline_run(id),
    step_id BIGINT NOT NULL REFERENCES mu_quiz.step(id),
    step_type INTEGER NOT NULL,
    alg_version INTEGER NOT NULL,
    step_cfg_data JSONB,
    status INTEGER NOT NULL,
    started_at TIMESTAMP WITH TIME ZONE,
    completed_at TIMESTAMP WITH TIME ZONE,
    input_table_name VARCHAR(255),
    result_table_name VARCHAR(255),
    result_stats JSONB,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create sequence for step_run
CREATE SEQUENCE mu_quiz.step_run_seq START 1 INCREMENT 1;

-- Create indexes
CREATE INDEX idx_step_run_pipeline_run_id ON mu_quiz.step_run(pipeline_run_id);
CREATE INDEX idx_step_run_step_id ON mu_quiz.step_run(step_id);
CREATE INDEX idx_step_run_status ON mu_quiz.step_run(status);

-- Add foreign key constraint for last_step_run_id in step table
ALTER TABLE mu_quiz.step ADD CONSTRAINT fk_step_last_step_run 
    FOREIGN KEY (last_step_run_id) REFERENCES mu_quiz.step_run(id);
