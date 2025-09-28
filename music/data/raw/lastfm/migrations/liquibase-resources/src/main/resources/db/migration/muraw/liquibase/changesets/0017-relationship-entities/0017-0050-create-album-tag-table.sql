-- Changeset: 003-create-album-tag-table
-- Author: refactoring

-- Create sequence for album_tag table
CREATE SEQUENCE album_tag_seq START WITH 1 INCREMENT BY 50;

-- Create album_tag table
CREATE TABLE album_tag (
        id                  BIGINT      NOT NULL DEFAULT nextval('album_tag_seq')
    ,   album_id            BIGINT      NOT NULL
    ,   tag_id              BIGINT      NOT NULL
    ,   usage_count         INTEGER
    ,   api_call_id         BIGINT      NOT NULL
    ,   approval_status     INTEGER     NOT NULL DEFAULT 1
    ,   created_at          TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
    ,   updated_at          TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
    ,   CONSTRAINT album_tag_PK
                               PRIMARY KEY (id)
    ,   CONSTRAINT album_tag_FK_album
                               FOREIGN KEY (album_id)
                                   REFERENCES album(id)
    ,   CONSTRAINT album_tag_FK_tag
                               FOREIGN KEY (tag_id)
                                   REFERENCES tag(id)
    ,   CONSTRAINT album_tag_FK_api_call
                               FOREIGN KEY (api_call_id)
                                   REFERENCES api_call(id)
);

-- Add unique constraint
ALTER TABLE album_tag ADD CONSTRAINT album_tag_UK_album_tag
    UNIQUE (album_id, tag_id);

-- Create indexes
CREATE INDEX album_tag_I_album
    ON album_tag (album_id);

CREATE INDEX album_tag_I_tag
    ON album_tag (tag_id);

