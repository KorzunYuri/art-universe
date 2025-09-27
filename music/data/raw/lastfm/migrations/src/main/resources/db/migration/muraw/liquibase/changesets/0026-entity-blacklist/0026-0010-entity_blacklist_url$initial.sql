-- Create blacklist_entity_url table for filtering unwanted entities by URL

CREATE SEQUENCE blacklist_entity_url_seq START 1 INCREMENT BY 50;

CREATE TABLE blacklist_entity_url (
    id          BIGINT          NOT NULL DEFAULT nextval('blacklist_entity_url_seq'),
    entity_type SMALLINT        NOT NULL,
    url         VARCHAR(8192)   NOT NULL,
    created_at  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT blacklist_entity_url_PK
        PRIMARY KEY (id),
    CONSTRAINT blacklist_entity_url_UK
        UNIQUE (entity_type, url)
);