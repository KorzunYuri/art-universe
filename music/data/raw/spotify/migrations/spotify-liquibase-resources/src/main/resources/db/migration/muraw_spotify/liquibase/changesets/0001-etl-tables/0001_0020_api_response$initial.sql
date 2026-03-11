CREATE TABLE api_response (
        id              BIGINT PRIMARY KEY DEFAULT nextval('api_response_seq')
    ,   api_call_id     BIGINT                              NOT NULL REFERENCES api_call(id)
    ,   status          SMALLINT                            NOT NULL DEFAULT 0
    ,   response_body   TEXT
    ,   http_status     INTEGER
    ,   error_message   VARCHAR(1024)
    ,   created_at      TIMESTAMPTZ                         NOT NULL DEFAULT now()
    ,   updated_at      TIMESTAMPTZ                         NOT NULL DEFAULT now()
);

CREATE SEQUENCE api_response_seq START 1 INCREMENT BY 50;

CREATE INDEX idx_api_response_status ON api_response (status);
CREATE INDEX idx_api_response_call ON api_response (api_call_id);
