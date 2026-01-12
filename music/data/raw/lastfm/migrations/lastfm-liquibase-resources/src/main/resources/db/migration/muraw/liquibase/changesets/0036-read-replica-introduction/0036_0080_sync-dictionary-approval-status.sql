INSERT INTO mu_raw_lastfm.dictionary (domain, code, name)
VALUES
    ('ApprovalStatus',1,'PENDING'),
    ('ApprovalStatus',2,'APPROVED'),
    ('ApprovalStatus',3,'DECLINED'),
    ('ApprovalStatus',4,'PRE_APPROVED'),
    ('ApprovalStatus',5,'IGNORED')
ON CONFLICT (domain, code) DO UPDATE SET
    name = EXCLUDED.name;
