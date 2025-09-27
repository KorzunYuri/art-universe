CREATE OR REPLACE PROCEDURE cleanup_history_add_message(
    IN  in_cleanup_run_id   BIGINT,
    IN  in_message          VARCHAR(1024)
)
    LANGUAGE plpgsql
AS $$
BEGIN

    RAISE NOTICE '%s', in_message;

    INSERT INTO cleanup_history
            (cleanup_run_id, message)
    VALUES  (in_cleanup_run_id, in_message);

END;
$$