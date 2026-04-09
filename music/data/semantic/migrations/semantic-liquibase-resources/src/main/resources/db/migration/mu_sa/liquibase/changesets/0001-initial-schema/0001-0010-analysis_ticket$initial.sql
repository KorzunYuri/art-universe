CREATE TABLE mu_semantic_analysis.analysis_ticket (
        id                      UUID            PRIMARY KEY DEFAULT gen_random_uuid()
    ,   data_source             SMALLINT        NOT NULL
    ,   subject_type            SMALLINT        NOT NULL
    ,   subject_id              BIGINT
    ,   subject_name            VARCHAR(256)    NOT NULL
    ,   text_samples            JSONB           NOT NULL
    ,   expected_proposal_types SMALLINT[]
    ,   expected_entity_types   SMALLINT[]
    ,   analysis_mode           SMALLINT        NOT NULL DEFAULT 1      -- FULL_EXTRACTION(1), CREATIVE_CATEGORIZATION(2)
    ,   status                  SMALLINT        NOT NULL DEFAULT 1
    ,   analysis_version        VARCHAR(32)
    ,   error_message           TEXT
    ,   created_at              TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
    ,   picked_up_at            TIMESTAMP
    ,   completed_at            TIMESTAMP
);

CREATE INDEX analysis_ticket_I_status
    ON mu_semantic_analysis.analysis_ticket (status)
    WHERE status = 1;

CREATE INDEX analysis_ticket_I_subject
    ON mu_semantic_analysis.analysis_ticket (subject_type, subject_id);
