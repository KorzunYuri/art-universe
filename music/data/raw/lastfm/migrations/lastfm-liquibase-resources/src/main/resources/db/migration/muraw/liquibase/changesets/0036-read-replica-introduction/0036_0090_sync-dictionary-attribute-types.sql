INSERT INTO mu_raw_lastfm.dictionary (domain, code, name)
VALUES
    ('LastfmAttribute',1,'Relations number'),
    ('LastfmAttribute',2,'Usage count'),
    ('LastfmAttribute',3,'URL'),
    ('LastfmAttribute',4,'rank'),
    ('LastfmAttribute',5,'mbid'),
    ('LastfmAttribute',6,'duration'),
    ('LastfmAttribute',7,'streamable'),
    ('LastfmAttribute',8,'on_tour'),
    ('LastfmAttribute',9,'listeners_count'),
    ('LastfmAttribute',10,'play_count'),
    ('LastfmAttribute',11,'match_coeff')
ON CONFLICT (domain, code) DO UPDATE SET
    name = EXCLUDED.name;
