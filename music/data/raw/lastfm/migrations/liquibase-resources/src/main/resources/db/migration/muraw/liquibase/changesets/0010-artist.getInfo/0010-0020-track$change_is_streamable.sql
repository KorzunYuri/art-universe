ALTER TABLE track
    ADD COLUMN is_streamable boolean;

UPDATE track
    SET is_streamable = (streamable = 1);

ALTER TABLE track
    DROP COLUMN streamable;
