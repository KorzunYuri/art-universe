-- Create pipeline_run table
CREATE TABLE mu_quiz.pipeline_run (
    id BIGSERIAL PRIMARY KEY,
    pipeline_id BIGINT NOT NULL REFERENCES mu_quiz.pipeline(id),
    status INTEGER NOT NULL REFERENCES mu_quiz.execution_status(code),
    started_at TIMESTAMP WITH TIME ZONE,
    completed_at TIMESTAMP WITH TIME ZONE,
    result_table_name VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create sequence for pipeline_run
CREATE SEQUENCE mu_quiz.pipeline_run_seq START 1 INCREMENT 1;

-- Create indexes
CREATE INDEX idx_pipeline_run_pipeline_id ON mu_quiz.pipeline_run(pipeline_id);
CREATE INDEX idx_pipeline_run_status ON mu_quiz.pipeline_run(status);
