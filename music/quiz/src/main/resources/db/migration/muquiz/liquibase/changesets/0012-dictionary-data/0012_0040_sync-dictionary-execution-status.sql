--liquibase formatted sql

--changeset system:0012_0040_sync-execution-status
--comment: Sync ExecutionStatus enum values to dictionary table

INSERT INTO mu_quiz.dictionary (domain, code, name)
VALUES
    ('ExecutionStatus',1,'PENDING'),
    ('ExecutionStatus',2,'STARTED'),
    ('ExecutionStatus',3,'COMPLETED'),
    ('ExecutionStatus',4,'FAILED')
ON CONFLICT (domain, code) DO UPDATE SET
    name = EXCLUDED.name;

--rollback DELETE FROM mu_quiz.dictionary WHERE type = 'ExecutionStatus';
