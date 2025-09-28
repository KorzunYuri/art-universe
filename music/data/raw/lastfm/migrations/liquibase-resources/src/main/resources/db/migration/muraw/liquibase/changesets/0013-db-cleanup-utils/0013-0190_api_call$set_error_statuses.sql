UPDATE  api_response
SET     status = 8   -- 'IS_ERROR_RESPONSE'
WHERE   jsonb_path_exists(response_body_json, '$.error');