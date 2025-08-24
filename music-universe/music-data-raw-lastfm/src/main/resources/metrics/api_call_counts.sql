-- Count of API calls by type and status, including combinations with 0 count
WITH all_combinations AS (
    SELECT 
        d_type.name as api_call_type,
        d_status.name as status
    FROM mu_raw_lastfm.dictionary d_type
    CROSS JOIN mu_raw_lastfm.dictionary d_status
    WHERE d_type.domain = 'ApiCallType' 
      AND d_status.domain = 'ApiCallStatus'
),
actual_counts AS (
    SELECT 
        d_type.name as api_call_type,
        d_status.name as status,
        COUNT(*) as count
    FROM mu_raw_lastfm.api_call ac
    JOIN mu_raw_lastfm.dictionary d_type ON d_type.code = ac.type AND d_type.domain = 'ApiCallType'
    JOIN mu_raw_lastfm.dictionary d_status ON d_status.code = ac.status AND d_status.domain = 'ApiCallStatus'
    GROUP BY d_type.name, d_status.name
)
SELECT 
    ac.api_call_type,
    ac.status,
    COALESCE(act.count, 0) as count
FROM all_combinations ac
LEFT JOIN actual_counts act ON ac.api_call_type = act.api_call_type AND ac.status = act.status
ORDER BY ac.api_call_type, ac.status;
