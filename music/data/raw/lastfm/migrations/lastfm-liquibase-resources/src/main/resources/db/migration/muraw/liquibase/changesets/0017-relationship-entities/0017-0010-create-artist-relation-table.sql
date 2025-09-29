-- Changeset: 001-create-artist-similarity-table
-- Author: refactoring

-- Create sequence for artist_artist table
CREATE SEQUENCE artist_artist_seq START WITH 1 INCREMENT BY 50;

-- Create artist_artist table
CREATE TABLE artist_artist (
        id                  BIGINT      NOT NULL DEFAULT nextval('artist_artist_seq')
    ,   source_artist_id    BIGINT      NOT NULL
    ,   target_artist_id    BIGINT      NOT NULL
    ,   match_score         DECIMAL(5,4)
    ,   relation_type       SMALLINT    NOT NULL
    ,   api_call_id         BIGINT      NOT NULL
    ,   approval_status     INTEGER     NOT NULL DEFAULT 1
    ,   created_at          TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
    ,   updated_at          TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
    ,   CONSTRAINT artist_artist_PK
                               PRIMARY KEY (id)
    ,   CONSTRAINT artist_artist_FK_source_artist
                               FOREIGN KEY (source_artist_id)
                                   REFERENCES artist(id)
    ,   CONSTRAINT artist_artist_FK_target_artist
                               FOREIGN KEY (target_artist_id)
                                   REFERENCES artist(id)
    ,   CONSTRAINT artist_artist_FK_api_call
                               FOREIGN KEY (api_call_id)
                                   REFERENCES api_call(id)
);

-- Add unique constraint
ALTER TABLE artist_artist ADD CONSTRAINT artist_artist_UK_source_target_relation
    UNIQUE (source_artist_id, target_artist_id, relation_type);

-- Create indexes
CREATE INDEX artist_artist_I_source_artist
    ON artist_artist (source_artist_id);

CREATE INDEX artist_artist_I_target_artist
    ON artist_artist (target_artist_id);
