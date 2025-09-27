ALTER TABLE api_call
ADD COLUMN data_snapshot_id BIGINT;

ALTER TABLE api_call
ADD CONSTRAINT api_call$data_snapshot_id_FK
    FOREIGN KEY (data_snapshot_id)
        REFERENCES data_snapshot (id)
        ON DELETE RESTRICT;
