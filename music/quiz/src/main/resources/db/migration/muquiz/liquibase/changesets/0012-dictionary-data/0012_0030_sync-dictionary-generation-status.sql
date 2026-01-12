--liquibase formatted sql

--changeset system:0012_0030_sync-generation-status
--comment: Sync GenerationStatus enum values to dictionary table

INSERT INTO mu_quiz.dictionary (domain, code, name)
VALUES
    ('GenerationStatus',1,'PENDING'),
    ('GenerationStatus',2,'COMPLETED'),
    ('GenerationStatus',3,'FAILED')
ON CONFLICT (domain, code) DO UPDATE SET
    name = EXCLUDED.name;

--rollback DELETE FROM mu_quiz.dictionary WHERE type = 'GenerationStatus';
