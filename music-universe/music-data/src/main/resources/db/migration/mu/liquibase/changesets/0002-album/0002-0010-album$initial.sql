CREATE TABLE album (
        id                  BIGINT
    ,   name                TEXT            NOT NULL
    ,   primary_artist_id   BIGINT          NOT NULL
    ,   album_group_id      BIGINT
    ,   release_date        DATE
    ,   created_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
    ,   updated_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
    ,   CONSTRAINT album_PK
            PRIMARY KEY (id)
    ,   CONSTRAINT album_FK_artist
            FOREIGN KEY (primary_artist_id)
                REFERENCES artist(id)
                ON DELETE RESTRICT
    ,   CONSTRAINT album_FK_album_group
            FOREIGN KEY (album_group_id)
                REFERENCES album(id)
                ON DELETE SET NULL
);
CREATE SEQUENCE album_seq START 1 INCREMENT BY 50;

CREATE INDEX album_I_primary_artist
    ON album (primary_artist_id);

CREATE INDEX album_I_album_group
    ON album (album_group_id);
