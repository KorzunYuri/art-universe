CREATE TABLE dictionary
(
    id          BIGSERIAL PRIMARY KEY,
    code        VARCHAR(255) NOT NULL,
    name        VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    type        VARCHAR(255) NOT NULL,
    CONSTRAINT dictionary_U_code_type UNIQUE (code, type)
);
