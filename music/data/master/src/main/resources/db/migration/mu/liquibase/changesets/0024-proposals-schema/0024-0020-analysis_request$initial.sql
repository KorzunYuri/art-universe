CREATE TABLE mu_proposals.analysis_request (
        id                  UUID            PRIMARY KEY
    ,   ticket_id           UUID            NOT NULL
            REFERENCES mu_proposals.analysis_ticket(id)
    ,   input_hash          VARCHAR(64)     NOT NULL
    ,   analysis_version    VARCHAR(32)     NOT NULL
    ,   data_source         SMALLINT        NOT NULL
    ,   subject_type        SMALLINT        NOT NULL
    ,   subject_id          BIGINT
    ,   status              SMALLINT        NOT NULL DEFAULT 1
    ,   llm_provider        VARCHAR(32)
    ,   llm_model           VARCHAR(64)
    ,   prompt_tokens       INTEGER
    ,   completion_tokens   INTEGER
    ,   raw_response        TEXT
    ,   error_message       TEXT
    ,   created_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
    ,   completed_at        TIMESTAMP
    ,   CONSTRAINT analysis_request_UQ_hash_version
            UNIQUE (input_hash, analysis_version)
);

CREATE INDEX analysis_request_I_subject
    ON mu_proposals.analysis_request (subject_type, subject_id);

CREATE INDEX analysis_request_I_status
    ON mu_proposals.analysis_request (status)
    WHERE status IN (1, 2);

CREATE INDEX analysis_request_I_version
    ON mu_proposals.analysis_request (analysis_version);
