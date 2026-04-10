CREATE TABLE mu_semantic_analysis.data_source (
        code        SMALLINT        PRIMARY KEY
    ,   name        VARCHAR(64)     NOT NULL UNIQUE
);

INSERT INTO mu_semantic_analysis.data_source (code, name) VALUES
        (1, 'lastfm')
    ,   (2, 'spotify')
    ,   (3, 'musicbrainz')
    ,   (4, 'master')
;
