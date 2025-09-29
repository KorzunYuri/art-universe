ALTER TABLE api_response ALTER COLUMN api_call_id DROP DEFAULT;
ALTER TABLE api_response ALTER COLUMN api_call_id TYPE BIGINT;
DROP SEQUENCE IF EXISTS api_response_api_call_id_seq;