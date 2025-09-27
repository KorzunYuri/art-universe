-- Changeset: 002-create-artist-tag-table
-- Author: refactoring

-- Create sequence for artist_tag table
CREATE SEQUENCE artist_tag_seq START WITH 1 INCREMENT BY 50;

-- Create artist_tag table
CREATE TABLE artist_tag (
        id                  BIGINT      NOT NULL DEFAULT nextval('artist_tag_seq')
    ,   artist_id           BIGINT      NOT NULL
    ,   tag_id              BIGINT      NOT NULL
    ,   usage_count         INTEGER
    ,   api_call_id         BIGINT      NOT NULL
    ,   approval_status     INTEGER     NOT NULL DEFAULT 1
    ,   created_at          TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
    ,   updated_at          TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
    ,   CONSTRAINT artist_tag_PK
                               PRIMARY KEY (id)
    ,   CONSTRAINT artist_tag_FK_artist
                               FOREIGN KEY (artist_id)
                                   REFERENCES artist(id)
    ,   CONSTRAINT artist_tag_FK_tag
                               FOREIGN KEY (tag_id)
                                   REFERENCES tag(id)
    ,   CONSTRAINT artist_tag_FK_api_call
                               FOREIGN KEY (api_call_id)
                                   REFERENCES api_call(id)
);

-- Add unique constraint
ALTER TABLE artist_tag ADD CONSTRAINT artist_tag_UK_artist_tag
    UNIQUE (artist_id, tag_id);

-- Create indexes
CREATE INDEX artist_tag_I_artist
    ON artist_tag (artist_id);

CREATE INDEX artist_tag_I_tag
    ON artist_tag (tag_id);

