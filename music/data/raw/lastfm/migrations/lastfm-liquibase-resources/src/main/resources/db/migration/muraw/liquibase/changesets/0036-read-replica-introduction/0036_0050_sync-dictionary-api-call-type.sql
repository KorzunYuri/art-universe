INSERT INTO mu_raw_lastfm.dictionary (domain, code, name)
VALUES
    ('ApiCallType',1,'TAG_TOP_TAGS'),
    ('ApiCallType',2,'TAG_TOP_ARTISTS'),
    ('ApiCallType',3,'TAG_TOP_TRACKS'),
    ('ApiCallType',4,'ARTIST_GET_INFO'),
    ('ApiCallType',5,'ARTIST_TOP_TAGS'),
    ('ApiCallType',6,'ARTIST_TOP_TRACKS'),
    ('ApiCallType',7,'ARTIST_TOP_ALBUMS'),
    ('ApiCallType',8,'ARTIST_GET_SIMILAR'),
    ('ApiCallType',9,'ARTIST_SEARCH'),
    ('ApiCallType',10,'TRACK_GET_INFO'),
    ('ApiCallType',11,'ALBUM_GET_INFO')
ON CONFLICT (domain, code) DO UPDATE SET
    name = EXCLUDED.name;
