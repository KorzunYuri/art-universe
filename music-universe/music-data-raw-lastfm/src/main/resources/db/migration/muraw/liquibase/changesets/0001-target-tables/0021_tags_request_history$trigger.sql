-- on insert into tags_request, insert a record into tags_request_history
CREATE OR REPLACE FUNCTION insert_tags_request_history()
    RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO tags_request_history (tag_request_id, from_dttm, to_dttm, status, created_at, updated_at)
    VALUES (NEW.id, NEW.from_dttm, NEW.to_dttm, NEW.status, NEW.created_at, NEW.updated_at);
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER tags_request_insert_trigger
    AFTER INSERT ON tags_request
    FOR EACH ROW
EXECUTE FUNCTION insert_tags_request_history();
