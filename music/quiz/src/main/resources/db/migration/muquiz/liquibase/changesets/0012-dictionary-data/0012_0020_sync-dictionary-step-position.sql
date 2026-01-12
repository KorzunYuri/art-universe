--liquibase formatted sql

--changeset system:0012_0020_sync-step-position
--comment: Sync StepPosition enum values to dictionary table

INSERT INTO mu_quiz.dictionary (domain, code, name)
VALUES
    ('StepPosition',1,'INITIAL'),
    ('StepPosition',2,'TRANSFORM'),
    ('StepPosition',3,'FINAL')
ON CONFLICT (domain, code) DO UPDATE SET
    name = EXCLUDED.name;

--rollback DELETE FROM mu_quiz.dictionary WHERE type = 'StepPosition';
