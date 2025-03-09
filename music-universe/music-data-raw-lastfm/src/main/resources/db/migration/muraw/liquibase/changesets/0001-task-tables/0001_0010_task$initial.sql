CREATE TABLE task
(
        id                  BIGSERIAL                           NOT NULL
    ,   task_type           SMALLINT                            NOT NULL
    ,   due_dttm            TIMESTAMP WITH TIME ZONE            NOT NULL
    ,   status              SMALLINT                            NOT NULL
    ,   attempt_cnt         SMALLINT  DEFAULT 0                 NOT NULL
    ,   created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
    ,   updated_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
    ,   CONSTRAINT task_PK
            PRIMARY KEY (id)
);
