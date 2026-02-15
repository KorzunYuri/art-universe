-- Create relation_type dictionary table
CREATE TABLE IF NOT EXISTS relation_type (
    id BIGINT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    reverse_name VARCHAR(255),
    is_symmetrical BOOLEAN NOT NULL DEFAULT FALSE,
    is_system BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT relation_type_UK_name UNIQUE (name)
);

-- Create sequence for relation_type
CREATE SEQUENCE IF NOT EXISTS relation_type_seq INCREMENT BY 50 START WITH 1;
