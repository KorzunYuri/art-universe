-- Changeset: 004-create-track-tag-table
-- Author: refactoring

-- Create sequence for track_tag table
CREATE SEQUENCE track_tag_seq START WITH 1 INCREMENT BY 50;

-- Create track_tag table
CREATE TABLE track_tag (
        id                  BIGINT      NOT NULL DEFAULT nextval('track_tag_seq')
    ,   track_id            BIGINT      NOT NULL
    ,   tag_id              BIGINT      NOT NULL
    ,   usage_count         INTEGER
    ,   api_call_id         BIGINT      NOT NULL
    ,   approval_status     INTEGER     NOT NULL DEFAULT 1
    ,   created_at          TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
    ,   updated_at          TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
    ,   CONSTRAINT track_tag_PK
                               PRIMARY KEY (id)
    ,   CONSTRAINT track_tag_FK_track
                               FOREIGN KEY (track_id)
                                   REFERENCES track(id)
    ,   CONSTRAINT track_tag_FK_tag
                               FOREIGN KEY (tag_id)
                                   REFERENCES tag(id)
    ,   CONSTRAINT track_tag_FK_api_call
                               FOREIGN KEY (api_call_id)
                                   REFERENCES api_call(id)
);

-- Add unique constraint
ALTER TABLE track_tag ADD CONSTRAINT track_tag_UK_track_tag
    UNIQUE (track_id, tag_id);

-- Create indexes
CREATE INDEX track_tag_I_track
    ON track_tag (track_id);

CREATE INDEX track_tag_I_tag
    ON track_tag (tag_id);
