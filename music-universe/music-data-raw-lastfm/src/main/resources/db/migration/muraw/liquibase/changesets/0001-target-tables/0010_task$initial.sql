CREATE TABLE task
(
        id                  SERIAL                              NOT NULL
    ,   task_type           VARCHAR(64)                         NOT NULL
    ,   due_dttm            TIMESTAMP WITH TIME ZONE            NOT NULL
    ,   status              SMALLINT                            NOT NULL
    ,   attempt_cnt         SMALLINT  DEFAULT 0                 NOT NULL
    ,   created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
    ,   updated_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
    ,   CONSTRAINT tags_request_PK
            PRIMARY KEY (id)
);
