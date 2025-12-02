-- Create pipeline_step table
CREATE TABLE mu_quiz.pipeline_step (
    id BIGSERIAL PRIMARY KEY,
    pipeline_id BIGINT NOT NULL REFERENCES mu_quiz.pipeline(id),
    step_id BIGINT NOT NULL REFERENCES mu_quiz.step(id),
    ord INTEGER NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create sequence for pipeline_step
CREATE SEQUENCE mu_quiz.pipeline_step_seq START 1 INCREMENT 1;

-- Create indexes
CREATE INDEX idx_pipeline_step_pipeline_id ON mu_quiz.pipeline_step(pipeline_id);
CREATE INDEX idx_pipeline_step_pipeline_ord ON mu_quiz.pipeline_step(pipeline_id, ord);
CREATE UNIQUE INDEX idx_pipeline_step_unique ON mu_quiz.pipeline_step(pipeline_id, step_id);
