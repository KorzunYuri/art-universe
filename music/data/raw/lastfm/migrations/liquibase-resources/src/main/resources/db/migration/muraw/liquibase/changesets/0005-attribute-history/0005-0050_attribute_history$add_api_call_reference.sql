ALTER TABLE attribute_history
ADD COLUMN api_call_id BIGINT;

ALTER TABLE attribute_history
ADD CONSTRAINT attribute_history$api_call_id_FK
    FOREIGN KEY (api_call_id)
        REFERENCES api_call (id)
        ON DELETE RESTRICT;