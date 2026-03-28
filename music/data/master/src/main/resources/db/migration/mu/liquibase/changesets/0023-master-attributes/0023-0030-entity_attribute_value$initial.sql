CREATE TABLE entity_attribute_value (
        id                  BIGINT          NOT NULL
    ,   target_type         SMALLINT        NOT NULL
    ,   target_id           BIGINT          NOT NULL
    ,   attribute_def_id    BIGINT          NOT NULL
    ,   numeric_value       BIGINT
    ,   string_value        VARCHAR(4096)
    ,   date_value          DATE
    ,   bool_value          BOOLEAN
    ,   event_date          DATE
    ,   valid_from          DATE
    ,   valid_till          DATE
    ,   source_type         SMALLINT        NOT NULL
    ,   source_ref          VARCHAR(256)
    ,   confidence          SMALLINT
    ,   origin              SMALLINT        NOT NULL DEFAULT 0
    ,   created_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
    ,   updated_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
    ,   CONSTRAINT entity_attribute_value_PK
            PRIMARY KEY (id)
    ,   CONSTRAINT entity_attribute_value_FK_def
            FOREIGN KEY (attribute_def_id)
                REFERENCES attribute_def(id)
);
CREATE SEQUENCE entity_attribute_value_seq START 1 INCREMENT BY 50;

CREATE INDEX entity_attribute_value_I_target
    ON entity_attribute_value (target_type, target_id);

CREATE INDEX entity_attribute_value_I_def
    ON entity_attribute_value (target_type, attribute_def_id);
