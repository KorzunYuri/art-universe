INSERT INTO mu_raw_lastfm.dictionary (domain, code, name)
VALUES
    ('ApiCallStatus',1,'CREATED'),
    ('ApiCallStatus',2,'PENDING'),
    ('ApiCallStatus',3,'EXPIRED'),
    ('ApiCallStatus',4,'CANCELLED'),
    ('ApiCallStatus',5,'PROCESSING'),
    ('ApiCallStatus',6,'SUCCESSFUL'),
    ('ApiCallStatus',7,'DUE_TO_RETRY'),
    ('ApiCallStatus',8,'FAILED')
ON CONFLICT (domain, code) DO UPDATE SET
    name = EXCLUDED.name;
