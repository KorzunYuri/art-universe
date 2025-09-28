-- Rename cleanup objects to mtnc_ (maintenance) prefix

-- 1. Rename tables
ALTER TABLE cleanup_run RENAME TO mtnc_cleanup_run;
ALTER TABLE cleanup_history RENAME TO mtnc_cleanup_history;

-- 2. Rename sequences
ALTER SEQUENCE cleanup_run_id_seq RENAME TO mtnc_cleanup_run_id_seq;

-- 3. Update foreign key constraint name
ALTER TABLE mtnc_cleanup_history 
    DROP CONSTRAINT cleanup_history_cleanup_run_id_fkey;

ALTER TABLE mtnc_cleanup_history 
    ADD CONSTRAINT mtnc_cleanup_history_cleanup_run_id_fkey 
    FOREIGN KEY (cleanup_run_id) REFERENCES mtnc_cleanup_run(id);

-- 4. Rename functions
-- First create new function with new name
CREATE OR REPLACE PROCEDURE mtnc_cleanup_history_add_message(
    in_cleanup_run_id BIGINT,
    in_message        TEXT
)
LANGUAGE plpgsql
AS $$
BEGIN
    INSERT INTO mtnc_cleanup_history (cleanup_run_id, message)
    VALUES (in_cleanup_run_id, in_message);
END;
$$;

-- Drop old function
DROP PROCEDURE IF EXISTS cleanup_history_add_message(BIGINT, TEXT);
