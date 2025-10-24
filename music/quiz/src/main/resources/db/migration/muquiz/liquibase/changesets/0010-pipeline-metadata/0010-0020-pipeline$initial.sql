-- Create pipeline table
CREATE TABLE mu_quiz.pipeline (
    id BIGSERIAL PRIMARY KEY,
    game_id BIGINT NOT NULL,
    immutable BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create sequence for pipeline
CREATE SEQUENCE mu_quiz.pipeline_seq START 1 INCREMENT 1;

-- Create index on game_id for faster lookups
CREATE UNIQUE INDEX idx_pipeline_game_id ON mu_quiz.pipeline(game_id);
