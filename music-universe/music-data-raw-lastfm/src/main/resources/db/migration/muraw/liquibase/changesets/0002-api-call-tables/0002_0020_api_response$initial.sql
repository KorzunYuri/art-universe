CREATE TABLE api_response (
        id                  BIGSERIAL
    ,   api_call_id         BIGSERIAL                           NOT NULL    REFERENCES api_call (id) ON DELETE RESTRICT
    ,   api_call_type       SMALLINT                            NOT NULL
    ,   status              SMALLINT                            NOT NULL
    ,   response_body       JSONB                               NOT NULL
    ,   created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
    ,   updated_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
    ,   CONSTRAINT api_response_PK
            PRIMARY KEY (id)
    ,   UNIQUE (api_call_id)
);
