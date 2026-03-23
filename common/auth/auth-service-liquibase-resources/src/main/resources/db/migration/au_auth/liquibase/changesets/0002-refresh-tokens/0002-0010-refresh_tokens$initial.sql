CREATE TABLE refresh_tokens
(
        id         BIGSERIAL                           NOT NULL
    ,   user_id    BIGINT                              NOT NULL
    ,   token      VARCHAR(512)                        NOT NULL
    ,   expires_at TIMESTAMP                           NOT NULL
    ,   created_at TIMESTAMP                           NOT NULL DEFAULT CURRENT_TIMESTAMP
    ,   CONSTRAINT refresh_tokens_PK          PRIMARY KEY (id)
    ,   CONSTRAINT refresh_tokens_UQ_token    UNIQUE (token)
    ,   CONSTRAINT refresh_tokens_FK_user_id  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX refresh_tokens_IDX_token      ON refresh_tokens (token);
CREATE INDEX refresh_tokens_IDX_user_id    ON refresh_tokens (user_id);
CREATE INDEX refresh_tokens_IDX_expires_at ON refresh_tokens (expires_at);
