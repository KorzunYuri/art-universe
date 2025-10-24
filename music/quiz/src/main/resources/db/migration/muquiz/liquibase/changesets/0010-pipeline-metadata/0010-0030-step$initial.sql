-- Create step table
CREATE TABLE mu_quiz.step (
    id BIGSERIAL PRIMARY KEY,
    type INTEGER NOT NULL REFERENCES mu_quiz.generation_step_type(code),
    alg_version INTEGER NOT NULL,
    cfg_data JSONB,
    preview_data JSONB,
    result_table_name VARCHAR(255),
    result_stats JSONB,
    last_step_run_id BIGINT,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    immutable BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create sequence for step
CREATE SEQUENCE mu_quiz.step_seq START 1 INCREMENT 1;

-- Create indexes
CREATE INDEX idx_step_type ON mu_quiz.step(type);
CREATE INDEX idx_step_deleted ON mu_quiz.step(deleted);
