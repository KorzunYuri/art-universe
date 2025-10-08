-- Changeset: 006-create-album-track-table
-- Author: refactoring

-- Create sequence for album_track table
CREATE SEQUENCE album_track_seq START WITH 1 INCREMENT BY 50;

-- Create album_track table
CREATE TABLE album_track (
        id                  BIGINT      NOT NULL DEFAULT nextval('album_track_seq')
    ,   album_id            BIGINT      NOT NULL
    ,   track_id            BIGINT      NOT NULL
    ,   position            INTEGER
    ,   api_call_id         BIGINT      NOT NULL
    ,   approval_status     INTEGER     NOT NULL DEFAULT 1
    ,   created_at          TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
    ,   updated_at          TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
    ,   CONSTRAINT album_track_PK
                               PRIMARY KEY (id)
    ,   CONSTRAINT album_track_FK_album
                               FOREIGN KEY (album_id)
                                   REFERENCES album(id)
    ,   CONSTRAINT album_track_FK_track
                               FOREIGN KEY (track_id)
                                   REFERENCES track(id)
    ,   CONSTRAINT album_track_FK_api_call
                               FOREIGN KEY (api_call_id)
                                   REFERENCES api_call(id)
);

-- Add unique constraint
ALTER TABLE album_track ADD CONSTRAINT album_track_UK_album_track
    UNIQUE (album_id, track_id);

-- Create indexes
CREATE INDEX album_track_I_album
    ON album_track (album_id);

CREATE INDEX album_track_I_track
    ON album_track (track_id);
