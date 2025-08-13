CREATE OR REPLACE VIEW mtnc_api_call_stats
AS
SELECT
        api_call_type
    ,	act.name 	as api_call_type_name
    , 	api_call_status
    , 	acs.name	as api_call_status_name
    , 	api_response_status
    , 	ars.name	as api_response_status_name
    ,	cnt
FROM
    (
        SELECT
                ac.type		as api_call_type
            , 	ac.status	as api_call_status
            , 	ar.status	as api_response_status
            , 	count(*)	as cnt
        from
            api_response ar
        join
            api_call ac
                on	ar.api_call_id = ac.id
        GROUP BY
                ac.type
            ,   ac.status
            ,   ar.status
    ) summary
JOIN
    dictionary act
        on		act.domain = 'ApiCallType'
            and act.code = api_call_type
JOIN
    dictionary acs
        on		acs.domain = 'ApiCallStatus'
            and acs.code = api_call_status
JOIN
    dictionary ars
        on		ars.domain = 'ApiResponseStatus'
            and ars.code = api_response_status
;