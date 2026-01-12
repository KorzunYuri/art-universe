--liquibase formatted sql

--changeset system:0012_0040_sync-execution-status
--comment: Sync ExecutionStatus enum values to dictionary table

INSERT INTO mu_quiz.dictionary (domain, code, name)
VALUES
    ('GenerationStepType',1,'APPROVED_FILTER'),
    ('GenerationStepType',2,'BLACKLIST_FILTER'),
    ('GenerationStepType',3,'WHITELIST_FILTER'),
    ('GenerationStepType',4,'TRACK_RECENCY_PENALTY'),
    ('GenerationStepType',5,'ARTIST_RECENCY_PENALTY'),
    ('GenerationStepType',6,'ARTIST_DIVERSITY'),
    ('GenerationStepType',7,'FINAL_SELECTION'),
    ('GenerationStepType',8,'FINAL_CATEGORIES_BALANCER')
ON CONFLICT (domain, code) DO UPDATE SET
    name = EXCLUDED.name;

--rollback DELETE FROM mu_quiz.dictionary WHERE type = 'ExecutionStatus';
