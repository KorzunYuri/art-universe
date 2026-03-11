CREATE SEQUENCE api_call_seq START 1 INCREMENT BY 50;

CREATE TABLE api_call (
        id              BIGINT PRIMARY KEY DEFAULT nextval('api_call_seq')
    ,   type            SMALLINT                            NOT NULL
    ,   status          SMALLINT                            NOT NULL DEFAULT 0
    ,   spotify_id      VARCHAR(64)
    ,   parameters      TEXT
    ,   entity_type     SMALLINT
    ,   entity_id       BIGINT
    ,   due_dttm        TIMESTAMPTZ                         NOT NULL DEFAULT now()
    ,   priority        SMALLINT                            NOT NULL DEFAULT 0
    ,   kafka_produced  BOOLEAN                             DEFAULT false
    ,   kafka_topic     VARCHAR(64)
    ,   created_at      TIMESTAMPTZ                         NOT NULL DEFAULT now()
    ,   updated_at      TIMESTAMPTZ                         NOT NULL DEFAULT now()
);

CREATE INDEX idx_api_call_status_type ON api_call (status, type);
CREATE INDEX idx_api_call_due ON api_call (due_dttm) WHERE status IN (0, 1);
CREATE INDEX idx_api_call_spotify_id ON api_call (spotify_id, type);
