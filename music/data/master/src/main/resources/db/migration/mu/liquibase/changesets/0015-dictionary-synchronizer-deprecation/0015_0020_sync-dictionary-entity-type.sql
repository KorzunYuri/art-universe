
INSERT INTO mu.dictionary (domain, code, name)
VALUES
    ('MasterEntityType',1,'artist'),
    ('MasterEntityType',2,'album'),
    ('MasterEntityType',3,'track'),
    ('MasterEntityType',4,'category'),
    ('MasterEntityType',5,'dimension')
ON CONFLICT (domain, code) DO UPDATE SET
    name = EXCLUDED.name;
