CREATE TABLE task_history
(
        task_id              INTEGER                             NOT NULL
    ,   task_type            VARCHAR(64)                         NOT NULL
    ,   due_dttm            TIMESTAMP WITH TIME ZONE            NOT NULL
    ,   status              SMALLINT                            NOT NULL
    ,   attempt_cnt         SMALLINT  DEFAULT 0                 NOT NULL
    ,   created_at          TIMESTAMP                           NOT NULL
    ,   updated_at          TIMESTAMP                           NOT NULL
);
