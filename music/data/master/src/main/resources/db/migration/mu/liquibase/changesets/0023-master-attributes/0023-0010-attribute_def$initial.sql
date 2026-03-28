CREATE TABLE attribute_def (
        id                  BIGINT          NOT NULL
    ,   code                VARCHAR(64)     NOT NULL
    ,   name                VARCHAR(128)    NOT NULL
    ,   description         TEXT
    ,   data_type           SMALLINT        NOT NULL
        -- 1=NUMERIC, 2=STRING, 3=DATE, 4=BOOLEAN
    ,   temporal_type       SMALLINT        NOT NULL
        -- 1=CONSTANT, 2=INSTANT, 3=PERIOD
    ,   computation_type    SMALLINT        NOT NULL DEFAULT 1
        -- 1=RAW, 2=DERIVED, 3=COMPOSITE, 4=SEMANTIC, 5=MANUAL
    ,   data_source         VARCHAR(32)
        -- 'lastfm','spotify' for RAW/DERIVED; 'multiple' for COMPOSITE; NULL for SEMANTIC/MANUAL
    ,   formula             TEXT
    ,   is_multi_value      BOOLEAN         NOT NULL DEFAULT FALSE
    ,   is_system           BOOLEAN         NOT NULL DEFAULT FALSE
    ,   created_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
    ,   updated_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
    ,   CONSTRAINT attribute_def_PK
            PRIMARY KEY (id)
    ,   CONSTRAINT attribute_def_UQ_code
            UNIQUE (code)
);
CREATE SEQUENCE attribute_def_seq START 1000 INCREMENT BY 1;
