-- on insert into task, insert a record into task_history
CREATE OR REPLACE FUNCTION insert_task_history()
    RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO task_history (
        task_id, task_type, due_dttm, status, attempt_cnt, created_at, updated_at
    )
    VALUES (
        NEW.id, NEW.task_type, NEW.due_dttm, NEW.status, NEW.attempt_cnt, NEW.created_at, NEW.updated_at
    );
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER task_insert_trigger
    AFTER INSERT ON task
    FOR EACH ROW
EXECUTE FUNCTION insert_task_history();
