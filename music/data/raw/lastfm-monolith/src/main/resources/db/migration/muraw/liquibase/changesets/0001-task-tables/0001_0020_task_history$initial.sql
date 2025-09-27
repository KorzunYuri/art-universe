CREATE TABLE task_history
(
        task_id             BIGSERIAL                           NOT NULL
    ,   task_type           SMALLINT                            NOT NULL
    ,   due_dttm            TIMESTAMP WITH TIME ZONE            NOT NULL
    ,   status              SMALLINT                            NOT NULL
    ,   attempt_cnt         SMALLINT  DEFAULT 0                 NOT NULL
    ,   created_at          TIMESTAMP                           NOT NULL
    ,   updated_at          TIMESTAMP                           NOT NULL
);
