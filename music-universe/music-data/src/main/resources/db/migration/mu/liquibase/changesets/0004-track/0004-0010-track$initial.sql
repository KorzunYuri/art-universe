CREATE TABLE track (
        id                  BIGINT
    ,   name                VARCHAR(1024)   NOT NULL
    ,   primary_artist_id   BIGINT          NOT NULL
    ,   track_group_id      BIGINT
    ,   created_at          TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
    ,   updated_at          TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
    ,   CONSTRAINT track_PK
            PRIMARY KEY (id)
    ,   CONSTRAINT track_FK_artist
            FOREIGN KEY (primary_artist_id)
                REFERENCES artist(id)
                ON DELETE RESTRICT
    ,   CONSTRAINT track_FK_track_group
            FOREIGN KEY (track_group_id)
                REFERENCES track(id)
                ON DELETE SET NULL
);
CREATE SEQUENCE track_seq START WITH 1 INCREMENT BY 50;

CREATE INDEX track_I_primary_artist
    ON track (primary_artist_id);

CREATE INDEX track_I_track_group
    ON track (track_group_id);

