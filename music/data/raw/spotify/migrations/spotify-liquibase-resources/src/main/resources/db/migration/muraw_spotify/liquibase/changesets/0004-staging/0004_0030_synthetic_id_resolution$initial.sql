CREATE TABLE synthetic_id_resolution (
        entity_type     SMALLINT                            NOT NULL
    ,   spotify_id      VARCHAR(256)                        NOT NULL
    ,   synthetic_id    BIGINT                              NOT NULL
    ,   real_id         BIGINT                              NOT NULL
    ,   resolved_at     TIMESTAMPTZ                         DEFAULT now()
    ,   PRIMARY KEY (entity_type, spotify_id)
);

CREATE INDEX idx_resolution_synthetic ON synthetic_id_resolution (synthetic_id);
CREATE INDEX idx_resolution_real ON synthetic_id_resolution (real_id);
