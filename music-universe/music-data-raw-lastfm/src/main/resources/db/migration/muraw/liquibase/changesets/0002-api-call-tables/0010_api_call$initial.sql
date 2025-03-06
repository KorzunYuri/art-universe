CREATE TABLE api_call (
        id                  BIGSERIAL
    ,   type                VARCHAR(255) NOT NULL
    ,   parameters          VARCHAR(1024) NOT NULL
    ,   status              VARCHAR(50) NOT NULL
    ,   due_dttm            TIMESTAMPTZ NOT NULL
    ,   created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
    ,   updated_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
    ,   CONSTRAINT api_call_PK
            PRIMARY KEY (id)
);
