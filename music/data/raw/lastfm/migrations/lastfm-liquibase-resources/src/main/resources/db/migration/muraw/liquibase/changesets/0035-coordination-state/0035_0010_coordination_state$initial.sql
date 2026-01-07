CREATE TABLE mu_raw_lastfm.coordination_state
(
        id                      SMALLINT                            NOT NULL
    ,   status                  SMALLINT                            NOT NULL
    ,   updated_at              TIMESTAMP WITH TIME ZONE            NOT NULL
    ,   updated_by_instance     VARCHAR(100)
    ,   CONSTRAINT coordination_state_PK
            PRIMARY KEY (id)
    ,   CONSTRAINT coordination_state$status_check
            CHECK (status IN (1, 2, 3))
);

COMMENT ON COLUMN mu_raw_lastfm.coordination_state.status IS '1=NORMAL, 2=REQUESTED, 3=RUNNING';

INSERT INTO mu_raw_lastfm.coordination_state (id, status, updated_at)
VALUES (1, 1, CURRENT_TIMESTAMP);

CREATE INDEX coordination_state_i_status
    ON mu_raw_lastfm.coordination_state(status);
