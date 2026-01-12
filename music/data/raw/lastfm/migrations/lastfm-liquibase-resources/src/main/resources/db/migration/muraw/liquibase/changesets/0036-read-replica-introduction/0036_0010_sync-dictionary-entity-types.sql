INSERT INTO mu_raw_lastfm.dictionary (domain, code, name)
VALUES
    ('LastfmEntityType',1,'ARTIST'),
    ('LastfmEntityType',2,'ALBUM'),
    ('LastfmEntityType',3,'TRACK'),
    ('LastfmEntityType',4,'TAG')
ON CONFLICT (domain, code) DO UPDATE SET
    name = EXCLUDED.name;
