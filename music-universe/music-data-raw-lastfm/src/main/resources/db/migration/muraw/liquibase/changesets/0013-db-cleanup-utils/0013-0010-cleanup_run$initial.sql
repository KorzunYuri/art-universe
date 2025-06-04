CREATE TABLE cleanup_run (
    id          BIGSERIAL   PRIMARY KEY,
    start_ts    TIMESTAMP   NOT NULL  DEFAULT CURRENT_TIMESTAMP,
    finish_ts   TIMESTAMP,
    dry_run     BOOLEAN     NOT NULL
);