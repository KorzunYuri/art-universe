-- Count of API responses by call type and response status, including combinations with 0 count
WITH all_combinations AS (
    SELECT 
        d_type.name as api_call_type,
        d_status.name as status
    FROM mu_raw_lastfm.dictionary d_type
    CROSS JOIN mu_raw_lastfm.dictionary d_status
    WHERE d_type.domain = 'ApiCallType' 
      AND d_status.domain = 'ApiResponseStatus'
),
actual_counts AS (
    SELECT 
        d_type.name as api_call_type,
        d_status.name as status,
        COUNT(*) as count
    FROM mu_raw_lastfm.api_response ar
    JOIN mu_raw_lastfm.api_call ac ON ac.id = ar.api_call_id
    JOIN mu_raw_lastfm.dictionary d_type ON d_type.code = ac.type AND d_type.domain = 'ApiCallType'
    JOIN mu_raw_lastfm.dictionary d_status ON d_status.code = ar.status AND d_status.domain = 'ApiResponseStatus'
    GROUP BY d_type.name, d_status.name
)
SELECT 
    ac.api_call_type,
    ac.status,
    COALESCE(act.count, 0) as count
FROM all_combinations ac
LEFT JOIN actual_counts act ON ac.api_call_type = act.api_call_type AND ac.status = act.status
ORDER BY ac.api_call_type, ac.status;
