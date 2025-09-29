ALTER TABLE tag
    ADD COLUMN api_call_id BIGINT;

ALTER TABLE tag
    ADD CONSTRAINT tag$api_call_id_FK
        FOREIGN KEY (api_call_id)
            REFERENCES api_call (id)
            ON DELETE RESTRICT;
