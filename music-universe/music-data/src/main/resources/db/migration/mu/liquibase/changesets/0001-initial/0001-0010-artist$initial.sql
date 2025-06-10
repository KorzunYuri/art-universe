CREATE TABLE IF NOT EXISTS mu.artist
(
        id          bigint NOT NULL
    ,   name        character varying(1024) COLLATE pg_catalog."default" NOT NULL
    ,   created_at  timestamp without time zone NOT NULL DEFAULT CURRENT_TIMESTAMP
    ,   updated_at  timestamp without time zone NOT NULL DEFAULT CURRENT_TIMESTAMP
    ,   CONSTRAINT artist_PK
            PRIMARY KEY (id)
    ,   UNIQUE (name)
);
CREATE SEQUENCE artist_seq START 1 INCREMENT BY 50;