CREATE TABLE tags_request_history
(
        tag_request_id      INTEGER                             NOT NULL
    ,   from_dttm           TIMESTAMP WITH TIME ZONE            NOT NULL
    ,   to_dttm             TIMESTAMP WITH TIME ZONE            NOT NULL
    ,   status              SMALLINT                            NOT NULL
    ,   created_at          TIMESTAMP                           NOT NULL
    ,   updated_at          TIMESTAMP                           NOT NULL
    ,   UNIQUE (tag_request_id, from_dttm, to_dttm, status)
);
