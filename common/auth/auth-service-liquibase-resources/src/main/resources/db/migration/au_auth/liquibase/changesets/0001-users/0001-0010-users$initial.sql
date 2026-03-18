CREATE TABLE users
(
        id            BIGSERIAL                           NOT NULL
    ,   email         VARCHAR(255)                        NOT NULL
    ,   name          VARCHAR(255)                        NOT NULL
    ,   picture_url   VARCHAR(1024)
    ,   google_sub    VARCHAR(255)                        NOT NULL
    ,   roles         TEXT[]                              NOT NULL DEFAULT '{VIEWER}'
    ,   enabled       BOOLEAN                             NOT NULL DEFAULT TRUE
    ,   created_at    TIMESTAMP                           NOT NULL DEFAULT CURRENT_TIMESTAMP
    ,   updated_at    TIMESTAMP                           NOT NULL DEFAULT CURRENT_TIMESTAMP
    ,   last_login_at TIMESTAMP
    ,   CONSTRAINT users_PK        PRIMARY KEY (id)
    ,   CONSTRAINT users_UQ_email  UNIQUE (email)
    ,   CONSTRAINT users_UQ_google UNIQUE (google_sub)
);
