CREATE OR REPLACE FUNCTION  cleanup_entity(
    in_entity_type   INTEGER,   -- is in fact SMALLINT, INTEGER is used for convenience
                                -- (otherwise you would have to cast arg to smallint when calling the function)
    in_threshold     INTEGER,
    in_dry_run       BOOLEAN
)
RETURNS SETOF cleanup_history
LANGUAGE plpgsql
AS $$
DECLARE
    v_root_table    TEXT;
    v_threshold_col TEXT;
    v_total         BIGINT;
    v_deleted       BIGINT;
    v_sql           TEXT;
    v_run_id        BIGINT;
BEGIN

    INSERT INTO cleanup_run
                (dry_run)
    VALUES      (in_dry_run)
    RETURNING   id
    INTO        v_run_id;

    -- 1) define entity table and threshold column by entity_type
    CASE in_entity_type
        WHEN 1 THEN
            v_root_table    := 'artist';
            v_threshold_col := 'listeners_count';
        WHEN 2 THEN
            v_root_table    := 'album';
            v_threshold_col := 'listeners_count';
        WHEN 3 THEN
            v_root_table    := 'track';
            v_threshold_col := 'play_count';
        WHEN 4 THEN
            v_root_table    := 'tag';
            v_threshold_col := 'usage_count';
        ELSE
            RAISE EXCEPTION 'Unsupported entity_type: %', in_entity_type;
    END CASE;

    DROP TABLE IF EXISTS tmp_entities_to_delete;
    CREATE TABLE tmp_entities_to_delete (
        id BIGINT PRIMARY KEY
    );

    v_sql := format(
    '   INSERT INTO tmp_entities_to_delete
        SELECT  id
        FROM    %I
        WHERE   %I < %s',
        v_root_table, v_threshold_col, in_threshold
    );
    execute v_sql;

    IF in_dry_run THEN
        -- count how much will be deleted

        -- 1) attribute_history
        SELECT  COUNT(*)
        INTO    v_deleted
        FROM    attribute_history
        WHERE   (       entity_type         = in_entity_type
                    AND entity_id           IN (SELECT id FROM tmp_entities_to_delete)  )
            OR  (       scope_entity_type   = in_entity_type
                    AND scope_entity_id     IN (SELECT id FROM tmp_entities_to_delete)  );

        SELECT  COUNT(*)
        INTO    v_total
        FROM    attribute_history;

        CALL cleanup_history_add_message(v_run_id, format('attribute_history: to remove %s out of %s (%s%%)',
                                                          v_deleted, v_total, ROUND(v_deleted::decimal / v_total * 100, 1)));

        -- 2) api_call
        SELECT  COUNT(*)
        INTO    v_deleted
        FROM    api_call
        WHERE   entity_type     = in_entity_type
          AND entity_id       IN (SELECT id FROM tmp_entities_to_delete)
          AND status          = 2; -- pending;

        SELECT COUNT(*)
        INTO    v_total
        FROM    api_call;

        CALL cleanup_history_add_message(v_run_id, format('api_call: %s related records (will not be deleted', v_deleted));

        -- 3) attribute_snapshot
        SELECT  COUNT(*)
        INTO    v_deleted
        FROM    attribute_snapshot
        WHERE   scope_entity_type   = in_entity_type
            AND scope_entity_id     IN (SELECT id FROM tmp_entities_to_delete) ;

        SELECT  COUNT(*)
        INTO    v_total
        FROM    attribute_snapshot;

        CALL cleanup_history_add_message(v_run_id, format('attribute_snapshot: to remove %s out of %s (%s%%)',
                                                          v_deleted, v_total, ROUND(v_deleted::decimal / v_total * 100, 1)));

        -- 4) data_snapshot
        SELECT  COUNT(*)
        INTO    v_deleted
        FROM    data_snapshot
        WHERE   entity_type         = in_entity_type
            AND entity_id           IN (SELECT id FROM tmp_entities_to_delete);

        SELECT  COUNT(*)
        INTO    v_total
        FROM    data_snapshot;

        CALL cleanup_history_add_message(v_run_id, format('data_snapshot: %s related records (will not be deleted', v_deleted));

        -- 5) entity_relation
        SELECT  COUNT(*)
        INTO    v_deleted
        FROM    entity_relation
        WHERE   (       entity_type         = in_entity_type
                    AND entity_id           IN (SELECT id FROM tmp_entities_to_delete)  )
            OR  (       scope_entity_type   = in_entity_type
                    AND scope_entity_id     IN (SELECT id FROM tmp_entities_to_delete)  );

        SELECT  COUNT(*)
        INTO    v_total
        FROM    entity_relation;

        CALL cleanup_history_add_message(v_run_id, format('entity_relation: to remove %s out of %s (%s%%)',
                                                          v_deleted, v_total, ROUND(v_deleted::decimal / v_total * 100, 1)));

        -- 6) root тable
        v_sql := format(
                'SELECT COUNT(*) FROM %I WHERE %I < %s',
                v_root_table, v_threshold_col, in_threshold);
        EXECUTE v_sql INTO v_deleted;

        EXECUTE format('SELECT COUNT(*) FROM %I', v_root_table)
        INTO    v_total;

        CALL cleanup_history_add_message(v_run_id, format('%I: to remove %s out of %s (%s%%)',
                                                          v_root_table, v_deleted, v_total, ROUND(v_deleted::decimal / v_total * 100, 1)));

    ELSE

        -- 1) attribute_history
        DELETE
        FROM    attribute_history
        WHERE   (       entity_type         = in_entity_type
                    AND entity_id           IN (SELECT id FROM tmp_entities_to_delete)  )
            OR  (       scope_entity_type   = in_entity_type
                    AND scope_entity_id     IN (SELECT id FROM tmp_entities_to_delete)  );
        GET DIAGNOSTICS v_deleted = ROW_COUNT;
        CALL cleanup_history_add_message(v_run_id, format('attribute_history: removed %s records', v_deleted));

        -- 2) api_call
        DELETE
        FROM    api_call
        WHERE   entity_type         = in_entity_type
            AND entity_id           IN (SELECT id FROM tmp_entities_to_delete)
            AND status = 2; -- pending
        GET DIAGNOSTICS v_deleted = ROW_COUNT;
        CALL cleanup_history_add_message(v_run_id, format('api_call: removed %s records', v_deleted));

        -- 3) attribute_snapshot
        DELETE
        FROM    attribute_snapshot
        WHERE   scope_entity_type   = in_entity_type
            AND scope_entity_id     IN (SELECT id FROM tmp_entities_to_delete) ;
        GET DIAGNOSTICS v_deleted = ROW_COUNT;
        CALL cleanup_history_add_message(v_run_id, format('attribute_snapshot: removed %s records', v_deleted));

        -- will not be cleaned as records are referencing completed api calls that are kept as well
        -- 4) data_snapshot
        -- DELETE
        -- FROM    data_snapshot
        -- WHERE   entity_type         = in_entity_type
        --     AND entity_id           IN (SELECT id FROM tmp_entities_to_delete);
        -- GET DIAGNOSTICS v_deleted = ROW_COUNT;
        -- CALL cleanup_history_add_message(v_run_id, format('data_snapshot: removed %s records', v_deleted));

        -- 5) entity_relation
        DELETE
        FROM    entity_relation
        WHERE   (       entity_type         = in_entity_type
            AND entity_id           IN (SELECT id FROM tmp_entities_to_delete)  )
           OR  (       scope_entity_type   = in_entity_type
            AND scope_entity_id     IN (SELECT id FROM tmp_entities_to_delete)  );
        GET DIAGNOSTICS v_deleted = ROW_COUNT;
        CALL cleanup_history_add_message(v_run_id, format('entity_relation: removed %s records', v_deleted));

        -- 6) root table
        v_sql := format(
                'DELETE FROM %I WHERE %I < %s',
                v_root_table, v_threshold_col, in_threshold);
        EXECUTE v_sql;
        GET DIAGNOSTICS v_deleted = ROW_COUNT;
        CALL cleanup_history_add_message(v_run_id, format('%s: removed %s records', v_root_table, v_deleted));

    END IF;

    -- cleanup helping table
    DROP TABLE IF EXISTS tmp_entities_to_delete;

    -- finalize cleanup run
    UPDATE  cleanup_run
    SET     finish_ts = now()
    WHERE   id = v_run_id;

    -- return cleanup history records for convenience
    RETURN QUERY
    SELECT  *
    FROM    cleanup_history
    WHERE   cleanup_run_id = v_run_id
    ORDER BY ts;

END;
$$;