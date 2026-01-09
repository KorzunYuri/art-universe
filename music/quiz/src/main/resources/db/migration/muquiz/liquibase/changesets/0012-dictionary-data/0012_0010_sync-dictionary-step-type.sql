--liquibase formatted sql

--changeset system:0012_0010_sync-step-type
--comment: Sync StepType enum values to dictionary table

INSERT INTO mu_quiz.dictionary (domain, code, name)
VALUES
    ('StepType',1,'APPROVED_FILTER'),
    ('StepType',2,'BLACKLIST_FILTER'),
    ('StepType',3,'WHITELIST_FILTER'),
    ('StepType',4,'TRACK_RECENCY_PENALTY'),
    ('StepType',5,'ARTIST_RECENCY_PENALTY'),
    ('StepType',6,'ARTIST_DIVERSITY'),
    ('StepType',7,'FINAL_LIMITER'),
    ('StepType',8,'FINAL_CATEGORIES_BALANCER'),
    ('StepType',9,'START_DATASOURCE')
ON CONFLICT (domain, code) DO UPDATE SET
    name = EXCLUDED.name;

--rollback DELETE FROM mu_quiz.dictionary WHERE type = 'StepType';
