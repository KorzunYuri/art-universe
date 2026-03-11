ALTER TABLE api_call ADD COLUMN executed_dttm TIMESTAMPTZ;
ALTER TABLE api_call ADD COLUMN http_status     INTEGER;
ALTER TABLE api_call ADD COLUMN error_message   VARCHAR(1024);