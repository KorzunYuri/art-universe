CREATE TABLE track (
        id                  BIGSERIAL                           NOT NULL
    ,   name                VARCHAR(1025)                       NOT NULL
    ,   mbid                VARCHAR(36)
    ,   url                 VARCHAR(1024)                       NOT NULL
    ,   duration            SMALLINT
    ,   streamable          SMALLINT
    ,   api_call_id         BIGINT
    ,   approval_status     SMALLINT                            NOT NULL
    ,   created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
    ,   updated_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
    ,   CONSTRAINT track_PK
            PRIMARY KEY (id)
    ,   UNIQUE (url)
    ,   CONSTRAINT track$api_call_id_FK
            FOREIGN KEY (api_call_id)
                REFERENCES api_call (id)
                ON DELETE RESTRICT
);;
CREATE SEQUENCE track_seq START 1 INCREMENT BY 50;