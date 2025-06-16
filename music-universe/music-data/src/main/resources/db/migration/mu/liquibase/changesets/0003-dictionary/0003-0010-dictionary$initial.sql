CREATE TABLE dictionary
(
        domain  VARCHAR(64) NOT NULL
    ,   code    SMALLINT    NOT NULL
    ,   name    VARCHAR(64) NOT NULL
    ,   CONSTRAINT dictionary_PK
            PRIMARY KEY (domain, code)
);
