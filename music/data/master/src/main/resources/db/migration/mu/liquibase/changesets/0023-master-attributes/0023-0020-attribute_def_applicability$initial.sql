CREATE TABLE attribute_def_applicability (
        id                  BIGINT          NOT NULL
    ,   attribute_def_id    BIGINT          NOT NULL
    ,   target_type         SMALLINT        NOT NULL
    ,   CONSTRAINT attribute_def_applicability_PK
            PRIMARY KEY (id)
    ,   CONSTRAINT attribute_def_applicability_FK_def
            FOREIGN KEY (attribute_def_id)
                REFERENCES attribute_def(id)
                ON DELETE CASCADE
    ,   CONSTRAINT attribute_def_applicability_UQ
            UNIQUE (attribute_def_id, target_type)
);
CREATE SEQUENCE attribute_def_applicability_seq START 1 INCREMENT BY 50;
