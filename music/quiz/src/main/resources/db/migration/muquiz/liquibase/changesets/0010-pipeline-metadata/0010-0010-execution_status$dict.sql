-- Create execution_status dictionary table
CREATE TABLE mu_quiz.execution_status (
    code INTEGER PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

-- Insert execution status values
INSERT INTO mu_quiz.execution_status (code, name) VALUES
    (1, 'PENDING'),
    (2, 'STARTED'),
    (3, 'COMPLETED'),
    (4, 'FAILED');
