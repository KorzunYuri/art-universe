CREATE TABLE cleanup_history (
    cleanup_run_id  BIGINT          NOT NULL REFERENCES cleanup_run(id),
    ts              TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    message         VARCHAR(1024)   NOT NULL
);