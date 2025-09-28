ALTER TABLE track
ADD COLUMN artist_id BIGINT;

ALTER TABLE track
ADD CONSTRAINT track$artist_id_FK
    FOREIGN KEY (artist_id)
        REFERENCES artist (id)
        ON DELETE RESTRICT;

CREATE INDEX track_I_artist_id ON track(artist_id);
