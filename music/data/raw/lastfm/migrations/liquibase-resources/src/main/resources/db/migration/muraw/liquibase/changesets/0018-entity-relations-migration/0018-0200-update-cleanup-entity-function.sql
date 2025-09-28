-- Changeset: 0018-0020-update-cleanup-entity-function
-- Author: yury_korzun
-- Description: Update cleanup_entity function to handle new relationship tables instead of deprecated entity_relation table

-- Create updated function
CREATE OR REPLACE FUNCTION cleanup_entity(
    in_entity_type integer,
    in_threshold integer,
    in_dry_run boolean)
    RETURNS SETOF cleanup_history
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE PARALLEL UNSAFE
    ROWS 1000

AS $BODY$
DECLARE
    v_root_table    TEXT;
    v_threshold_col TEXT;
    v_total         BIGINT;
    v_deleted       BIGINT;
    v_sql           TEXT;
    v_run_id        BIGINT;
BEGIN

    INSERT INTO cleanup_run (dry_run)
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
            v_threshold_col := 'play_count';
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
                WHERE   approval_status <> 2
                    AND %I < %s',
            v_root_table, v_threshold_col, in_threshold
             );
    execute v_sql;
    CREATE INDEX ON tmp_entities_to_delete (id);
    ANALYZE tmp_entities_to_delete;

    SELECT  COUNT(*)
    INTO    v_deleted
    FROM    tmp_entities_to_delete;

    CALL cleanup_history_add_message(v_run_id, format('entities to remove: %s', v_deleted));

    IF in_dry_run THEN
        -- count how much will be deleted

        -- 1) attribute_history
        SELECT  COUNT(*)
        INTO    v_total
        FROM    attribute_history;

        SELECT  COUNT(*)
        INTO    v_deleted
        FROM
            attribute_history ah
        JOIN
            tmp_entities_to_delete tmp
                ON  	ah.entity_type         = in_entity_type
                    AND ah.entity_id           = tmp.id;

        CALL cleanup_history_add_message(v_run_id, format('attribute_history: to remove %s out of %s (%s%%) (entity records)',
                                                          v_deleted, v_total, ROUND(v_deleted::decimal / v_total * 100, 1)));

        SELECT  COUNT(*)
        INTO    v_deleted
        FROM
            attribute_history ah
        JOIN
            tmp_entities_to_delete tmp
                ON   	ah.scope_entity_type   = in_entity_type
                    AND ah.scope_entity_id     = tmp.id;

        CALL cleanup_history_add_message(v_run_id, format('attribute_history: to remove %s out of %s (%s%%) (records with entity as a scope)',
                                                          v_deleted, v_total, ROUND(v_deleted::decimal / v_total * 100, 1)));

        -- 2) api_call
        SELECT  COUNT(*)
        INTO    v_deleted
        FROM
            api_call c
        JOIN
                tmp_entities_to_delete tmp
                ON   	c.entity_type     = in_entity_type
                    AND c.entity_id       = tmp.id
                    AND c.status          = 2; -- pending;

        SELECT COUNT(*)
        INTO    v_total
        FROM    api_call;

        CALL cleanup_history_add_message(v_run_id, format('api_call: %s related records (will not be deleted)', v_deleted));

        -- 3) attribute_snapshot
        SELECT  COUNT(*)
        INTO    v_deleted
        FROM
            attribute_snapshot s
        JOIN
            tmp_entities_to_delete tmp
                ON		s.scope_entity_type   = in_entity_type
                    AND s.scope_entity_id     = tmp.id;

        SELECT  COUNT(*)
        INTO    v_total
        FROM    attribute_snapshot;

        CALL cleanup_history_add_message(v_run_id, format('attribute_snapshot: to remove %s out of %s (%s%%)',
                                                          v_deleted, v_total, ROUND(v_deleted::decimal / v_total * 100, 1)));

        -- 4) data_snapshot
        SELECT  COUNT(*)
        INTO    v_deleted
        FROM
            data_snapshot s
        JOIN
            tmp_entities_to_delete tmp
                ON		s.entity_type         = in_entity_type
                    AND s.entity_id           = tmp.id;

        SELECT  COUNT(*)
        INTO    v_total
        FROM    data_snapshot;

        CALL cleanup_history_add_message(v_run_id, format('data_snapshot: %s related records (will not be deleted)', v_deleted));

        -- 5) Relationship tables based on entity type
        -- 5.1) Artist relationships (entity_type = 1)
        IF in_entity_type = 1 THEN
            -- artist_artist (as source)
            SELECT COUNT(*) INTO v_total FROM artist_artist;
            SELECT COUNT(*) INTO v_deleted
            FROM artist_artist aa
            JOIN tmp_entities_to_delete tmp ON aa.source_artist_id = tmp.id;
            
            CALL cleanup_history_add_message(v_run_id, format('artist_artist: to remove %s out of %s (%s%%) (as source artist)',
                                                             v_deleted, v_total, ROUND(v_deleted::decimal / v_total * 100, 1)));
            
            -- artist_artist (as target)
            SELECT COUNT(*) INTO v_deleted
            FROM artist_artist aa
            JOIN tmp_entities_to_delete tmp ON aa.target_artist_id = tmp.id;
            
            CALL cleanup_history_add_message(v_run_id, format('artist_artist: to remove %s out of %s (%s%%) (as target artist)',
                                                             v_deleted, v_total, ROUND(v_deleted::decimal / v_total * 100, 1)));
            
            -- artist_album
            SELECT COUNT(*) INTO v_total FROM artist_album;
            SELECT COUNT(*) INTO v_deleted
            FROM artist_album aa
            JOIN tmp_entities_to_delete tmp ON aa.artist_id = tmp.id;
            
            CALL cleanup_history_add_message(v_run_id, format('artist_album: to remove %s out of %s (%s%%)',
                                                             v_deleted, v_total, ROUND(v_deleted::decimal / v_total * 100, 1)));
            
            -- artist_track
            SELECT COUNT(*) INTO v_total FROM artist_track;
            SELECT COUNT(*) INTO v_deleted
            FROM artist_track at
            JOIN tmp_entities_to_delete tmp ON at.artist_id = tmp.id;
            
            CALL cleanup_history_add_message(v_run_id, format('artist_track: to remove %s out of %s (%s%%)',
                                                             v_deleted, v_total, ROUND(v_deleted::decimal / v_total * 100, 1)));
            
            -- artist_tag
            SELECT COUNT(*) INTO v_total FROM artist_tag;
            SELECT COUNT(*) INTO v_deleted
            FROM artist_tag at
            JOIN tmp_entities_to_delete tmp ON at.artist_id = tmp.id;
            
            CALL cleanup_history_add_message(v_run_id, format('artist_tag: to remove %s out of %s (%s%%)',
                                                             v_deleted, v_total, ROUND(v_deleted::decimal / v_total * 100, 1)));
        
        -- 5.2) Album relationships (entity_type = 2)
        ELSIF in_entity_type = 2 THEN
            -- artist_album
            SELECT COUNT(*) INTO v_total FROM artist_album;
            SELECT COUNT(*) INTO v_deleted
            FROM artist_album aa
            JOIN tmp_entities_to_delete tmp ON aa.album_id = tmp.id;
            
            CALL cleanup_history_add_message(v_run_id, format('artist_album: to remove %s out of %s (%s%%)',
                                                             v_deleted, v_total, ROUND(v_deleted::decimal / v_total * 100, 1)));
            
            -- album_track
            SELECT COUNT(*) INTO v_total FROM album_track;
            SELECT COUNT(*) INTO v_deleted
            FROM album_track at
            JOIN tmp_entities_to_delete tmp ON at.album_id = tmp.id;
            
            CALL cleanup_history_add_message(v_run_id, format('album_track: to remove %s out of %s (%s%%)',
                                                             v_deleted, v_total, ROUND(v_deleted::decimal / v_total * 100, 1)));
            
            -- album_tag
            SELECT COUNT(*) INTO v_total FROM album_tag;
            SELECT COUNT(*) INTO v_deleted
            FROM album_tag at
            JOIN tmp_entities_to_delete tmp ON at.album_id = tmp.id;
            
            CALL cleanup_history_add_message(v_run_id, format('album_tag: to remove %s out of %s (%s%%)',
                                                             v_deleted, v_total, ROUND(v_deleted::decimal / v_total * 100, 1)));
        
        -- 5.3) Track relationships (entity_type = 3)
        ELSIF in_entity_type = 3 THEN
            -- artist_track
            SELECT COUNT(*) INTO v_total FROM artist_track;
            SELECT COUNT(*) INTO v_deleted
            FROM artist_track at
            JOIN tmp_entities_to_delete tmp ON at.track_id = tmp.id;
            
            CALL cleanup_history_add_message(v_run_id, format('artist_track: to remove %s out of %s (%s%%)',
                                                             v_deleted, v_total, ROUND(v_deleted::decimal / v_total * 100, 1)));
            
            -- album_track
            SELECT COUNT(*) INTO v_total FROM album_track;
            SELECT COUNT(*) INTO v_deleted
            FROM album_track at
            JOIN tmp_entities_to_delete tmp ON at.track_id = tmp.id;
            
            CALL cleanup_history_add_message(v_run_id, format('album_track: to remove %s out of %s (%s%%)',
                                                             v_deleted, v_total, ROUND(v_deleted::decimal / v_total * 100, 1)));
            
            -- track_tag
            SELECT COUNT(*) INTO v_total FROM track_tag;
            SELECT COUNT(*) INTO v_deleted
            FROM track_tag tt
            JOIN tmp_entities_to_delete tmp ON tt.track_id = tmp.id;
            
            CALL cleanup_history_add_message(v_run_id, format('track_tag: to remove %s out of %s (%s%%)',
                                                             v_deleted, v_total, ROUND(v_deleted::decimal / v_total * 100, 1)));
        
        -- 5.4) Tag relationships (entity_type = 4)
        ELSIF in_entity_type = 4 THEN
            -- artist_tag
            SELECT COUNT(*) INTO v_total FROM artist_tag;
            SELECT COUNT(*) INTO v_deleted
            FROM artist_tag at
            JOIN tmp_entities_to_delete tmp ON at.tag_id = tmp.id;
            
            CALL cleanup_history_add_message(v_run_id, format('artist_tag: to remove %s out of %s (%s%%)',
                                                             v_deleted, v_total, ROUND(v_deleted::decimal / v_total * 100, 1)));
            
            -- album_tag
            SELECT COUNT(*) INTO v_total FROM album_tag;
            SELECT COUNT(*) INTO v_deleted
            FROM album_tag at
            JOIN tmp_entities_to_delete tmp ON at.tag_id = tmp.id;
            
            CALL cleanup_history_add_message(v_run_id, format('album_tag: to remove %s out of %s (%s%%)',
                                                             v_deleted, v_total, ROUND(v_deleted::decimal / v_total * 100, 1)));
            
            -- track_tag
            SELECT COUNT(*) INTO v_total FROM track_tag;
            SELECT COUNT(*) INTO v_deleted
            FROM track_tag tt
            JOIN tmp_entities_to_delete tmp ON tt.tag_id = tmp.id;
            
            CALL cleanup_history_add_message(v_run_id, format('track_tag: to remove %s out of %s (%s%%)',
                                                             v_deleted, v_total, ROUND(v_deleted::decimal / v_total * 100, 1)));
        END IF;

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
        CALL cleanup_history_add_message(v_run_id, 'cleanup attribute_history started');

        DELETE
        FROM    attribute_history ah
        USING 	tmp_entities_to_delete tmp
        WHERE   ah.entity_type         = in_entity_type
          AND ah.entity_id           = tmp.id;

        GET DIAGNOSTICS v_deleted = ROW_COUNT;
        CALL cleanup_history_add_message(v_run_id, format('attribute_history: removed %s records', v_deleted));

        DELETE
        FROM    attribute_history ah
        USING 	tmp_entities_to_delete tmp
        WHERE   ah.scope_entity_type   = in_entity_type
          AND ah.scope_entity_id     = tmp.id;

        GET DIAGNOSTICS v_deleted = ROW_COUNT;
        CALL cleanup_history_add_message(v_run_id, format('attribute_history: removed %s records with entity as a scope', v_deleted));

        -- 2) api_call
        CALL cleanup_history_add_message(v_run_id, 'cleanup api_call started');

        UPDATE 	api_call
        SET 	status = 4  -- cancelled
        FROM  	tmp_entities_to_delete tmp
        WHERE   entity_type         = in_entity_type
          AND entity_id           = tmp.id
          AND status = 2; -- pending

        GET DIAGNOSTICS v_deleted = ROW_COUNT;
        CALL cleanup_history_add_message(v_run_id, format('api_call: cancelled %s records', v_deleted));

        -- 3) attribute_snapshot
        CALL cleanup_history_add_message(v_run_id, 'cleanup attribute_snapshot started');

        DELETE
        FROM    attribute_snapshot s
            USING 	tmp_entities_to_delete tmp
        WHERE   s.scope_entity_type   = in_entity_type
          AND s.scope_entity_id     = tmp.id;

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

        -- 5) Relationship tables based on entity type
        CALL cleanup_history_add_message(v_run_id, 'cleanup relationship tables started');
        
        -- 5.1) Artist relationships (entity_type = 1)
        IF in_entity_type = 1 THEN
            -- artist_artist (as source)
            DELETE FROM artist_artist aa
            USING tmp_entities_to_delete tmp
            WHERE aa.source_artist_id = tmp.id;
            
            GET DIAGNOSTICS v_deleted = ROW_COUNT;
            CALL cleanup_history_add_message(v_run_id, format('artist_artist: removed %s records (as source artist)', v_deleted));
            
            -- artist_artist (as target)
            DELETE FROM artist_artist aa
            USING tmp_entities_to_delete tmp
            WHERE aa.target_artist_id = tmp.id;
            
            GET DIAGNOSTICS v_deleted = ROW_COUNT;
            CALL cleanup_history_add_message(v_run_id, format('artist_artist: removed %s records (as target artist)', v_deleted));
            
            -- artist_album
            DELETE FROM artist_album aa
            USING tmp_entities_to_delete tmp
            WHERE aa.artist_id = tmp.id;
            
            GET DIAGNOSTICS v_deleted = ROW_COUNT;
            CALL cleanup_history_add_message(v_run_id, format('artist_album: removed %s records', v_deleted));
            
            -- artist_track
            DELETE FROM artist_track at
            USING tmp_entities_to_delete tmp
            WHERE at.artist_id = tmp.id;
            
            GET DIAGNOSTICS v_deleted = ROW_COUNT;
            CALL cleanup_history_add_message(v_run_id, format('artist_track: removed %s records', v_deleted));
            
            -- artist_tag
            DELETE FROM artist_tag at
            USING tmp_entities_to_delete tmp
            WHERE at.artist_id = tmp.id;
            
            GET DIAGNOSTICS v_deleted = ROW_COUNT;
            CALL cleanup_history_add_message(v_run_id, format('artist_tag: removed %s records', v_deleted));
        
        -- 5.2) Album relationships (entity_type = 2)
        ELSIF in_entity_type = 2 THEN
            -- artist_album
            DELETE FROM artist_album aa
            USING tmp_entities_to_delete tmp
            WHERE aa.album_id = tmp.id;
            
            GET DIAGNOSTICS v_deleted = ROW_COUNT;
            CALL cleanup_history_add_message(v_run_id, format('artist_album: removed %s records', v_deleted));
            
            -- album_track
            DELETE FROM album_track at
            USING tmp_entities_to_delete tmp
            WHERE at.album_id = tmp.id;
            
            GET DIAGNOSTICS v_deleted = ROW_COUNT;
            CALL cleanup_history_add_message(v_run_id, format('album_track: removed %s records', v_deleted));
            
            -- album_tag
            DELETE FROM album_tag at
            USING tmp_entities_to_delete tmp
            WHERE at.album_id = tmp.id;
            
            GET DIAGNOSTICS v_deleted = ROW_COUNT;
            CALL cleanup_history_add_message(v_run_id, format('album_tag: removed %s records', v_deleted));
        
        -- 5.3) Track relationships (entity_type = 3)
        ELSIF in_entity_type = 3 THEN
            -- artist_track
            DELETE FROM artist_track at
            USING tmp_entities_to_delete tmp
            WHERE at.track_id = tmp.id;
            
            GET DIAGNOSTICS v_deleted = ROW_COUNT;
            CALL cleanup_history_add_message(v_run_id, format('artist_track: removed %s records', v_deleted));
            
            -- album_track
            DELETE FROM album_track at
            USING tmp_entities_to_delete tmp
            WHERE at.track_id = tmp.id;
            
            GET DIAGNOSTICS v_deleted = ROW_COUNT;
            CALL cleanup_history_add_message(v_run_id, format('album_track: removed %s records', v_deleted));
            
            -- track_tag
            DELETE FROM track_tag tt
            USING tmp_entities_to_delete tmp
            WHERE tt.track_id = tmp.id;
            
            GET DIAGNOSTICS v_deleted = ROW_COUNT;
            CALL cleanup_history_add_message(v_run_id, format('track_tag: removed %s records', v_deleted));
        
        -- 5.4) Tag relationships (entity_type = 4)
        ELSIF in_entity_type = 4 THEN
            -- artist_tag
            DELETE FROM artist_tag at
            USING tmp_entities_to_delete tmp
            WHERE at.tag_id = tmp.id;
            
            GET DIAGNOSTICS v_deleted = ROW_COUNT;
            CALL cleanup_history_add_message(v_run_id, format('artist_tag: removed %s records', v_deleted));
            
            -- album_tag
            DELETE FROM album_tag at
            USING tmp_entities_to_delete tmp
            WHERE at.tag_id = tmp.id;
            
            GET DIAGNOSTICS v_deleted = ROW_COUNT;
            CALL cleanup_history_add_message(v_run_id, format('album_tag: removed %s records', v_deleted));
            
            -- track_tag
            DELETE FROM track_tag tt
            USING tmp_entities_to_delete tmp
            WHERE tt.tag_id = tmp.id;
            
            GET DIAGNOSTICS v_deleted = ROW_COUNT;
            CALL cleanup_history_add_message(v_run_id, format('track_tag: removed %s records', v_deleted));
        END IF;

        -- 6) root table
        CALL cleanup_history_add_message(v_run_id, 'cleanup root table started');

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
$BODY$;

-- Add comment to function
COMMENT ON FUNCTION cleanup_entity(integer, integer, boolean)
    IS 'Cleans up entities with approval_status <> 2 and threshold value below specified. Updated to handle new relationship tables instead of deprecated entity_relation table.';
