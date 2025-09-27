ALTER TABLE api_response
    RENAME COLUMN response_body TO response_body_json;

ALTER TABLE api_response
    ADD COLUMN response_body TEXT;

ALTER TABLE api_response
    ADD COLUMN is_response_encoded BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE api_response
    ALTER COLUMN response_body_json SET DEFAULT '{}';