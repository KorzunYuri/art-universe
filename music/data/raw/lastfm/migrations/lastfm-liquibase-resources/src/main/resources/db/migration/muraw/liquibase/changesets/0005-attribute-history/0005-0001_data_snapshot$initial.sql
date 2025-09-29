CREATE TABLE data_snapshot (
        id                  BIGSERIAL
    ,   api_call_type       SMALLINT                            NOT NULL
    ,   data_date           DATE                                NOT NULL
    ,   entity_type         SMALLINT
    ,   entity_id           BIGSERIAL
    ,   created_cnt         INTEGER   DEFAULT 0                 NOT NULL
    ,   completed_cnt       INTEGER   DEFAULT 0                 NOT NULL
    ,   parsed_cnt          INTEGER   DEFAULT 0                 NOT NULL
    ,   created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
    ,   updated_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
    ,   CONSTRAINT data_snapshot_PK
            PRIMARY KEY (id)
    ,   UNIQUE (api_call_type, data_date, entity_type, entity_id)
);
CREATE SEQUENCE data_snapshot_seq START 1 INCREMENT BY 50;