-- Add pipeline_run_id column to generation table
ALTER TABLE mu_quiz.generation ADD COLUMN pipeline_run_id BIGINT;

-- Add foreign key constraint
ALTER TABLE mu_quiz.generation ADD CONSTRAINT fk_generation_pipeline_run 
    FOREIGN KEY (pipeline_run_id) REFERENCES mu_quiz.pipeline_run(id);

-- Create index for faster lookups
CREATE INDEX idx_generation_pipeline_run_id ON mu_quiz.generation(pipeline_run_id);
