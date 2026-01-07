CREATE TABLE attribute_history (
        id                  BIGSERIAL                           NOT NULL
    ,   entity_type_id      SMALLINT                            NOT NULL
    ,   entity_id           BIGSERIAL                           NOT NULL
    ,   attribute_id        SMALLINT                            NOT NULL
    ,   string_value        VARCHAR(4096)
    ,   int_value           INTEGER
    ,   collection_ts       TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
    ,   CONSTRAINT attribute_history_PK
            PRIMARY KEY (id)
    ,   CONSTRAINT attribute_history_FK_attribute_id
            FOREIGN KEY (attribute_id) REFERENCES attribute (id)
);
CREATE SEQUENCE attribute_history_seq START 1 INCREMENT BY 50;