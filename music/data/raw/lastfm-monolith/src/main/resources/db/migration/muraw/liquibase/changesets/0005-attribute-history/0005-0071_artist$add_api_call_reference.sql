ALTER TABLE artist
    ADD COLUMN api_call_id BIGINT;

ALTER TABLE artist
    ADD CONSTRAINT artist$api_call_id_FK
        FOREIGN KEY (api_call_id)
            REFERENCES api_call (id)
            ON DELETE RESTRICT;
