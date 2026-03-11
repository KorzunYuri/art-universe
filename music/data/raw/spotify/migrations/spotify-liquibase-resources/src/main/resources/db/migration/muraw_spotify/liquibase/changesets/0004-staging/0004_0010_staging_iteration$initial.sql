CREATE TABLE staging_iteration (
        id                  BIGINT PRIMARY KEY DEFAULT nextval('staging_iteration_seq')
    ,   status              SMALLINT                            NOT NULL DEFAULT 0
    ,   records_staged      BIGINT                              DEFAULT 0
    ,   records_applied     BIGINT                              DEFAULT 0
    ,   records_failed      BIGINT                              DEFAULT 0
    ,   opened_at           TIMESTAMPTZ                         NOT NULL DEFAULT now()
    ,   sealed_at           TIMESTAMPTZ
    ,   applied_at          TIMESTAMPTZ
    ,   error_message       TEXT
    ,   retry_count         SMALLINT                            DEFAULT 0
    ,   last_retry_at       TIMESTAMPTZ
);

CREATE SEQUENCE staging_iteration_seq START 1 INCREMENT BY 1;
