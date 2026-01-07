DROP SEQUENCE IF EXISTS game_seq;

DROP TABLE IF EXISTS game;

CREATE TABLE game (
    id BIGSERIAL PRIMARY KEY,
    generation_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE SEQUENCE game_seq START 1 INCREMENT BY 50;

COMMENT ON TABLE game IS 'Quiz games played';
COMMENT ON COLUMN game.generation_id IS 'Currently approved generation for this game';
