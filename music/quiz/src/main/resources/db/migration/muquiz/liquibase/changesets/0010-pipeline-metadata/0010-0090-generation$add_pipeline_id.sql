-- Add pipeline_id column to generation table (nullable for backward compatibility)
ALTER TABLE mu_quiz.generation ADD COLUMN pipeline_id BIGINT;

-- Add foreign key constraint
ALTER TABLE mu_quiz.generation ADD CONSTRAINT fk_generation_pipeline 
    FOREIGN KEY (pipeline_id) REFERENCES mu_quiz.pipeline(id);

-- Create index for faster lookups
CREATE INDEX idx_generation_pipeline_id ON mu_quiz.generation(pipeline_id);
