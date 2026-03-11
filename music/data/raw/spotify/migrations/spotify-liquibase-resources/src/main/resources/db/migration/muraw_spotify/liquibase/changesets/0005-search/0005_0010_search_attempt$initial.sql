CREATE SEQUENCE search_attempt_seq START 1 INCREMENT BY 50;

CREATE TABLE search_attempt (
    id                  BIGINT          PRIMARY KEY DEFAULT nextval('search_attempt_seq'),
    entity_type         SMALLINT        NOT NULL,
    master_entity_id    BIGINT          NOT NULL,
    search_string       VARCHAR(1024)   NOT NULL,
    status              SMALLINT        NOT NULL DEFAULT 1,
    best_match_score    DOUBLE PRECISION,
    matched_spotify_id  VARCHAR(64),
    api_call_id         BIGINT          REFERENCES api_call(id),
    searched_at         TIMESTAMPTZ,
    next_retry_after    TIMESTAMPTZ,
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT now(),
    UNIQUE (entity_type, master_entity_id)
);

CREATE INDEX idx_search_attempt_status ON search_attempt (status);
CREATE INDEX idx_search_attempt_api_call ON search_attempt (api_call_id);
