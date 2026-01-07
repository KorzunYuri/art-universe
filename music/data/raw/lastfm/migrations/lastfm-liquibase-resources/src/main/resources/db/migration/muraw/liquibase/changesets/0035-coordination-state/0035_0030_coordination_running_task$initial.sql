CREATE TABLE mu_raw_lastfm.coordination_running_task
(
        task_key                VARCHAR(100)                        NOT NULL
    ,   instance_id             VARCHAR(100)                        NOT NULL
    ,   acquired_at             TIMESTAMP WITH TIME ZONE            NOT NULL
    ,   CONSTRAINT coordination_running_task_PK
            PRIMARY KEY (task_key)
    ,   CONSTRAINT coordination_running_task$instance_FK
            FOREIGN KEY (instance_id)
            REFERENCES mu_raw_lastfm.coordination_instance(instance_id)
            ON DELETE CASCADE
);

CREATE INDEX coordination_running_task_i_instance
    ON mu_raw_lastfm.coordination_running_task(instance_id);

CREATE INDEX coordination_running_task_i_acquired
    ON mu_raw_lastfm.coordination_running_task(acquired_at);
