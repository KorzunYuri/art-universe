CREATE TABLE attribute_history_current (
        id                  BIGINT PRIMARY KEY DEFAULT nextval('attr_hist_seq')
    ,   entity_type         SMALLINT                            NOT NULL
    ,   entity_id           BIGINT                              NOT NULL
    ,   attribute_id        SMALLINT                            NOT NULL
    ,   scope_entity_type   SMALLINT
    ,   scope_entity_id     BIGINT
    ,   string_value        VARCHAR(4096)
    ,   numeric_value       BIGINT
    ,   api_call_id         BIGINT
    ,   collection_ts       TIMESTAMPTZ
    ,   valid_from          DATE                                NOT NULL
    ,   UNIQUE (entity_type, entity_id, attribute_id, scope_entity_type, scope_entity_id)
);

CREATE TABLE attribute_history_archive (
        id                  BIGINT PRIMARY KEY DEFAULT nextval('attr_hist_seq')
    ,   entity_type         SMALLINT                            NOT NULL
    ,   entity_id           BIGINT                              NOT NULL
    ,   attribute_id        SMALLINT                            NOT NULL
    ,   scope_entity_type   SMALLINT
    ,   scope_entity_id     BIGINT
    ,   string_value        VARCHAR(4096)
    ,   numeric_value       BIGINT
    ,   api_call_id         BIGINT
    ,   collection_ts       TIMESTAMPTZ
    ,   valid_from          DATE                                NOT NULL
    ,   valid_till          DATE                                NOT NULL
);

CREATE SEQUENCE attr_hist_seq START 1 INCREMENT BY 50;

CREATE INDEX idx_attr_hist_current_entity ON attribute_history_current (entity_type, entity_id);
CREATE INDEX idx_attr_hist_archive_entity ON attribute_history_archive (entity_type, entity_id);
