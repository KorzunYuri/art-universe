CREATE TABLE tags_request
(
        id                  SERIAL                              NOT NULL
    ,   from_dttm           TIMESTAMP WITH TIME ZONE            NOT NULL
    ,   to_dttm             TIMESTAMP WITH TIME ZONE            NOT NULL
    ,   status              SMALLINT                            NOT NULL
    ,   created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
    ,   updated_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
    ,   CONSTRAINT tags_request_PK
            PRIMARY KEY (id)
    ,   UNIQUE (from_dttm)
);
