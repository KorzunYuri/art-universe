-- Add pipeline_id column to game table
ALTER TABLE mu_quiz.game ADD COLUMN pipeline_id BIGINT;

-- Add foreign key constraint (will be enforced after data migration)
-- ALTER TABLE mu_quiz.game ADD CONSTRAINT fk_game_pipeline 
--     FOREIGN KEY (pipeline_id) REFERENCES mu_quiz.pipeline(id);

-- Create index for faster lookups
CREATE INDEX idx_game_pipeline_id ON mu_quiz.game(pipeline_id);
