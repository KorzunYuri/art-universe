INSERT INTO mu_raw_lastfm.dictionary (domain, code, name)
VALUES
    ('ApiResponseStatus',1,'CREATED'),
    ('ApiResponseStatus',2,'PENDING'),
    ('ApiResponseStatus',3,'PROCESSING'),
    ('ApiResponseStatus',4,'PROCESSING_ERROR'),
    ('ApiResponseStatus',5,'COMPLETED'),
    ('ApiResponseStatus',6,'SCHEDULED'),
    ('ApiResponseStatus',7,'VALIDATION_ERROR'),
    ('ApiResponseStatus',8,'IS_ERROR_RESPONSE')
ON CONFLICT (domain, code) DO UPDATE SET
    name = EXCLUDED.name;
