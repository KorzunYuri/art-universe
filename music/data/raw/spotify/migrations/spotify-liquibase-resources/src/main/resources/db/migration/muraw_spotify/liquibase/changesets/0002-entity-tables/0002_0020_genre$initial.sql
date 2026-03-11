CREATE SEQUENCE genre_seq START 1 INCREMENT BY 50;

CREATE TABLE genre (
        id                  BIGINT PRIMARY KEY DEFAULT nextval('genre_seq')
    ,   spotify_id          VARCHAR(256)                        NOT NULL UNIQUE
    ,   name                VARCHAR(256)                        NOT NULL
    ,   api_call_id         BIGINT
    ,   approval_status     SMALLINT                            NOT NULL DEFAULT 1
    ,   created_at          TIMESTAMPTZ                         NOT NULL DEFAULT now()
    ,   updated_at          TIMESTAMPTZ                         NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX idx_genre_name ON genre (name);
