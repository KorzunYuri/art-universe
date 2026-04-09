CREATE TABLE mu_semantic_analysis.proposal (
        id                  BIGINT          NOT NULL
    ,   request_id          UUID            NOT NULL
            REFERENCES mu_semantic_analysis.analysis_request(id)
    ,   synth_id            VARCHAR(16)
    ,   proposal_type       SMALLINT        NOT NULL
    ,   subject_type        SMALLINT        NOT NULL
    ,   subject_id          BIGINT
    ,   subject_ref         VARCHAR(16)
    ,   confidence          SMALLINT        NOT NULL
    ,   reasoning           TEXT            NOT NULL
    ,   payload             JSONB           NOT NULL
    ,   resolution          SMALLINT        NOT NULL DEFAULT 1
    ,   resolved_by         VARCHAR(64)
    ,   resolved_at         TIMESTAMP
    ,   applied_ref         VARCHAR(256)
    ,   created_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
    ,   CONSTRAINT proposal_PK
            PRIMARY KEY (id)
);

CREATE SEQUENCE mu_semantic_analysis.proposal_seq START 1 INCREMENT BY 50;

CREATE INDEX proposal_I_request
    ON mu_semantic_analysis.proposal (request_id);

CREATE INDEX proposal_I_pending
    ON mu_semantic_analysis.proposal (resolution)
    WHERE resolution = 1;

CREATE INDEX proposal_I_type
    ON mu_semantic_analysis.proposal (proposal_type, resolution);

CREATE INDEX proposal_I_subject
    ON mu_semantic_analysis.proposal (subject_type, subject_id);
