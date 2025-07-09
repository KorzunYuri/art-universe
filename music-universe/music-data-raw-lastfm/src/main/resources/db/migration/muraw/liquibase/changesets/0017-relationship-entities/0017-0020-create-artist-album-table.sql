-- Changeset: 005-create-artist-album-table
-- Author: refactoring

-- Create sequence for artist_album table
CREATE SEQUENCE artist_album_seq START WITH 1 INCREMENT BY 50;

-- Create artist_album table
CREATE TABLE artist_album (
        id                  BIGINT      NOT NULL DEFAULT nextval('artist_album_seq')
    ,   artist_id           BIGINT      NOT NULL
    ,   album_id            BIGINT      NOT NULL
    ,   api_call_id         BIGINT      NOT NULL
    ,   approval_status     INTEGER     NOT NULL DEFAULT 1
    ,   created_at          TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
    ,   updated_at          TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
    ,   CONSTRAINT artist_album_PK
                               PRIMARY KEY (id)
    ,   CONSTRAINT artist_album_FK_artist
                               FOREIGN KEY (artist_id)
                                   REFERENCES artist(id)
    ,   CONSTRAINT artist_album_FK_album
                               FOREIGN KEY (album_id)
                                   REFERENCES album(id)
    ,   CONSTRAINT artist_album_FK_api_call
                               FOREIGN KEY (api_call_id)
                                   REFERENCES api_call(id)
);

-- Add unique constraint
ALTER TABLE artist_album ADD CONSTRAINT artist_album_UK_artist_album
    UNIQUE (artist_id, album_id);

-- Create indexes
CREATE INDEX artist_album_I_artist
    ON artist_album (artist_id);

CREATE INDEX artist_album_I_album
    ON artist_album (album_id);
