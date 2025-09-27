-- Changeset: 007-create-artist-track-table
-- Author: refactoring

-- Create sequence for artist_track table
CREATE SEQUENCE artist_track_seq START WITH 1 INCREMENT BY 50;

-- Create artist_track table
CREATE TABLE artist_track (
        id                  BIGINT      NOT NULL DEFAULT nextval('artist_track_seq')
    ,   artist_id           BIGINT      NOT NULL
    ,   track_id            BIGINT      NOT NULL
    ,   api_call_id         BIGINT      NOT NULL
    ,   approval_status     INTEGER     NOT NULL DEFAULT 1
    ,   created_at          TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
    ,   updated_at          TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
    ,   CONSTRAINT artist_track_PK
                               PRIMARY KEY (id)
    ,   CONSTRAINT artist_track_FK_artist
                               FOREIGN KEY (artist_id)
                                   REFERENCES artist(id)
    ,   CONSTRAINT artist_track_FK_track
                               FOREIGN KEY (track_id)
                                   REFERENCES track(id)
    ,   CONSTRAINT artist_track_FK_api_call
                               FOREIGN KEY (api_call_id)
                                   REFERENCES api_call(id)
);

-- Add unique constraint
ALTER TABLE artist_track ADD CONSTRAINT artist_track_UK_artist_track
    UNIQUE (artist_id, track_id);

-- Create indexes
CREATE INDEX artist_track_I_artist
    ON artist_track (artist_id);

CREATE INDEX artist_track_I_track
    ON artist_track (track_id);
