DROP SEQUENCE IF EXISTS generation_seq;

DROP INDEX IF EXISTS idx_generation_game_id;

DROP TABLE IF EXISTS generation;

CREATE TABLE generation (
    id BIGSERIAL PRIMARY KEY,
    game_id BIGINT NOT NULL REFERENCES game(id),
    target_count INTEGER NOT NULL,
    status SMALLINT NOT NULL,
    result_table_name VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE SEQUENCE generation_seq START 1 INCREMENT BY 50;

CREATE INDEX idx_generation_game_id ON generation(game_id);

COMMENT ON TABLE generation IS 'Track generations for quiz games';
COMMENT ON COLUMN generation.target_count IS 'Target number of tracks to generate';
COMMENT ON COLUMN generation.status IS 'Generation status: 1=PENDING, 2=COMPLETED, 3=FAILED';
COMMENT ON COLUMN generation.result_table_name IS 'Name of staging table with results';
