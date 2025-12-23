CREATE TABLE mu_raw_lastfm.coordination_instance
(
        instance_id             VARCHAR(100)                        NOT NULL
    ,   module_name             VARCHAR(50)                         NOT NULL
    ,   host_name               VARCHAR(255)
    ,   start_time              TIMESTAMP WITH TIME ZONE            NOT NULL
    ,   last_heartbeat          TIMESTAMP WITH TIME ZONE            NOT NULL
    ,   CONSTRAINT coordination_instance_PK
            PRIMARY KEY (instance_id)
);

CREATE INDEX coordination_instance_i_heartbeat
    ON mu_raw_lastfm.coordination_instance(last_heartbeat);

CREATE INDEX coordination_instance_i_module
    ON mu_raw_lastfm.coordination_instance(module_name);
