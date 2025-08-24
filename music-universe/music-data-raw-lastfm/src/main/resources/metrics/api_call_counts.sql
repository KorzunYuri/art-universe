-- Count of API calls by type and status
SELECT 
    d_type.name as api_call_type,
    d_status.name as status,
    COUNT(*) as count
FROM mu_raw_lastfm.api_call ac
JOIN mu_raw_lastfm.dictionary d_type ON d_type.code = ac.type AND d_type.domain = 'ApiCallType'
JOIN mu_raw_lastfm.dictionary d_status ON d_status.code = ac.status AND d_status.domain = 'ApiCallStatus'
GROUP BY d_type.name, d_status.name
ORDER BY d_type.name, d_status.name;
