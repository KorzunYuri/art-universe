-- Drop old cleanup_entity function
DROP FUNCTION IF EXISTS cleanup_entity(INTEGER, INTEGER, BOOLEAN);

-- Create new unified maintenance cleanup function
CREATE OR REPLACE FUNCTION mtnc_cleanup_entity(
    in_entity_type   INTEGER,   -- 1=artist, 2=album, 3=track, 4=tag
    in_threshold     INTEGER,
    in_dry_run       BOOLEAN
)
    RETURNS SETOF mtnc_cleanup_history
    LANGUAGE plpgsql
AS $$
DECLARE
    v_root_table        TEXT;
    v_threshold_col     TEXT;
    v_entity_name       TEXT;
    v_total             BIGINT;
    v_deleted           BIGINT;
    v_sql               TEXT;
    v_run_id            BIGINT;
    v_relationship_rec  RECORD;
    v_relationship_sql  TEXT;
    v_count_sql         TEXT;
    v_percentage        NUMERIC;
BEGIN

    -- Create cleanup run record
    INSERT INTO mtnc_cleanup_run (dry_run)
    VALUES (in_dry_run)
    RETURNING id INTO v_run_id;

    -- Define entity table, threshold column and name by entity_type
    CASE in_entity_type
        WHEN 1 THEN
            v_root_table    := 'artist';
            v_threshold_col := 'listeners_count';
            v_entity_name   := 'artist';
        WHEN 2 THEN
            v_root_table    := 'album';
            v_threshold_col := 'play_count';
            v_entity_name   := 'album';
        WHEN 3 THEN
            v_root_table    := 'track';
            v_threshold_col := 'play_count';
            v_entity_name   := 'track';
        WHEN 4 THEN
            v_root_table    := 'tag';
            v_threshold_col := 'usage_count';
            v_entity_name   := 'tag';
        ELSE
            RAISE EXCEPTION 'Unsupported entity_type: %', in_entity_type;
        END CASE;

    -- Create temporary table with entities to delete (only PENDING status = 1)
    DROP TABLE IF EXISTS tmp_entities_to_delete;
    CREATE TABLE tmp_entities_to_delete (
                                            id BIGINT PRIMARY KEY
    );

    v_sql := format(
            'INSERT INTO tmp_entities_to_delete
             SELECT id
             FROM %I
             WHERE approval_status = 1
               AND %I < %s',
            v_root_table, v_threshold_col, in_threshold
             );
    EXECUTE v_sql;

    CREATE INDEX ON tmp_entities_to_delete (id);
    ANALYZE tmp_entities_to_delete;

    SELECT COUNT(*) INTO v_deleted FROM tmp_entities_to_delete;
    CALL mtnc_cleanup_history_add_message(v_run_id, format('entities to remove: %s', v_deleted));

    IF in_dry_run THEN
        -- DRY RUN MODE - Count what would be deleted

        -- 1) attribute_history
        SELECT COUNT(*) INTO v_total FROM attribute_history;

        SELECT COUNT(*) INTO v_deleted
        FROM attribute_history ah
                 JOIN tmp_entities_to_delete tmp ON ah.entity_type = in_entity_type AND ah.entity_id = tmp.id;

        -- Calculate percentage safely
        v_percentage := CASE WHEN v_total > 0 THEN ROUND(v_deleted::decimal / v_total * 100, 1) ELSE 0 END;

        CALL mtnc_cleanup_history_add_message(v_run_id, format('attribute_history: to remove %s out of %s (%s%%) (entity records)',
                                                               v_deleted, v_total, v_percentage));

        SELECT COUNT(*) INTO v_deleted
        FROM attribute_history ah
                 JOIN tmp_entities_to_delete tmp ON ah.scope_entity_type = in_entity_type AND ah.scope_entity_id = tmp.id;

        -- Calculate percentage safely
        v_percentage := CASE WHEN v_total > 0 THEN ROUND(v_deleted::decimal / v_total * 100, 1) ELSE 0 END;

        CALL mtnc_cleanup_history_add_message(v_run_id, format('attribute_history: to remove %s out of %s (%s%%) (records with entity as scope)',
                                                               v_deleted, v_total, v_percentage));

        -- 2) api_call
        SELECT COUNT(*) INTO v_deleted
        FROM api_call c
                 JOIN tmp_entities_to_delete tmp ON c.entity_type = in_entity_type AND c.entity_id = tmp.id AND c.status = 2; -- pending

        CALL mtnc_cleanup_history_add_message(v_run_id, format('api_call: %s pending records will be cancelled', v_deleted));

        -- 3) attribute_snapshot - will become inaccessible but not deleted
        SELECT COUNT(*) INTO v_total FROM attribute_snapshot;
        SELECT COUNT(*) INTO v_deleted
        FROM attribute_snapshot s
                 JOIN tmp_entities_to_delete tmp ON s.scope_entity_type = in_entity_type AND s.scope_entity_id = tmp.id;

        CALL mtnc_cleanup_history_add_message(v_run_id, format('attribute_snapshot: %s records will become inaccessible (not deleted to preserve data_snapshot integrity)',
                                                               v_deleted));

        -- 4) Direct foreign key references (track.artist_id, album.artist_id)
        -- These need to be handled before relationship tables for artists
        IF in_entity_type = 1 THEN -- artist cleanup
            -- Count tracks referencing artists to be deleted
            SELECT COUNT(*) INTO v_total FROM track;
            SELECT COUNT(*) INTO v_deleted
            FROM track t
                     JOIN tmp_entities_to_delete tmp ON t.artist_id = tmp.id;

            v_percentage := CASE WHEN v_total > 0 THEN ROUND(v_deleted::decimal / v_total * 100, 1) ELSE 0 END;
            CALL mtnc_cleanup_history_add_message(v_run_id, format('track: to remove %s out of %s (%s%%) (tracks with deleted artist)',
                                                                   v_deleted, v_total, v_percentage));

            -- Count albums referencing artists to be deleted
            SELECT COUNT(*) INTO v_total FROM album;
            SELECT COUNT(*) INTO v_deleted
            FROM album a
                     JOIN tmp_entities_to_delete tmp ON a.artist_id = tmp.id;

            v_percentage := CASE WHEN v_total > 0 THEN ROUND(v_deleted::decimal / v_total * 100, 1) ELSE 0 END;
            CALL mtnc_cleanup_history_add_message(v_run_id, format('album: to remove %s out of %s (%s%%) (albums with deleted artist)',
                                                                   v_deleted, v_total, v_percentage));
        END IF;

        -- 5) Relationship tables - unified approach
        -- Generate relationship table analysis dynamically
        FOR v_relationship_rec IN
            SELECT
                table_name,
                column_name,
                CASE
                    WHEN table_name = 'artist_artist' AND column_name = 'source_artist_id' THEN 'as source'
                    WHEN table_name = 'artist_artist' AND column_name = 'target_artist_id' THEN 'as target'
                    ELSE ''
                    END as role_suffix
            FROM (
                     -- Artist relationships (entity_type = 1)
                     SELECT 'artist_tag' as table_name, 'artist_id' as column_name WHERE in_entity_type = 1
                     UNION ALL SELECT 'artist_album', 'artist_id' WHERE in_entity_type = 1
                     UNION ALL SELECT 'artist_track', 'artist_id' WHERE in_entity_type = 1
                     UNION ALL SELECT 'artist_artist', 'source_artist_id' WHERE in_entity_type = 1
                     UNION ALL SELECT 'artist_artist', 'target_artist_id' WHERE in_entity_type = 1
                     -- Album relationships (entity_type = 2)
                     UNION ALL SELECT 'artist_album', 'album_id' WHERE in_entity_type = 2
                     UNION ALL SELECT 'album_track', 'album_id' WHERE in_entity_type = 2
                     UNION ALL SELECT 'album_tag', 'album_id' WHERE in_entity_type = 2
                     -- Track relationships (entity_type = 3)
                     UNION ALL SELECT 'artist_track', 'track_id' WHERE in_entity_type = 3
                     UNION ALL SELECT 'album_track', 'track_id' WHERE in_entity_type = 3
                     UNION ALL SELECT 'track_tag', 'track_id' WHERE in_entity_type = 3
                     -- Tag relationships (entity_type = 4)
                     UNION ALL SELECT 'artist_tag', 'tag_id' WHERE in_entity_type = 4
                     UNION ALL SELECT 'album_tag', 'tag_id' WHERE in_entity_type = 4
                     UNION ALL SELECT 'track_tag', 'tag_id' WHERE in_entity_type = 4
                 ) relationships
            LOOP
                v_count_sql := format('SELECT COUNT(*) FROM %I', v_relationship_rec.table_name);
                EXECUTE v_count_sql INTO v_total;

                v_count_sql := format(
                        'SELECT COUNT(*) FROM %I r JOIN tmp_entities_to_delete tmp ON r.%I = tmp.id',
                        v_relationship_rec.table_name, v_relationship_rec.column_name
                               );
                EXECUTE v_count_sql INTO v_deleted;

                -- Calculate percentage safely
                v_percentage := CASE WHEN v_total > 0 THEN ROUND(v_deleted::decimal / v_total * 100, 1) ELSE 0 END;

                CALL mtnc_cleanup_history_add_message(v_run_id, format('%s: to remove %s out of %s (%s%%) %s',
                                                                       v_relationship_rec.table_name, v_deleted, v_total,
                                                                       v_percentage,
                                                                       v_relationship_rec.role_suffix));
            END LOOP;

        -- 6) Root table
        EXECUTE format('SELECT COUNT(*) FROM %I', v_root_table) INTO v_total;
        EXECUTE format('SELECT COUNT(*) FROM %I WHERE approval_status = 1 AND %I < %s',
                       v_root_table, v_threshold_col, in_threshold) INTO v_deleted;

        -- Calculate percentage safely
        v_percentage := CASE WHEN v_total > 0 THEN ROUND(v_deleted::decimal / v_total * 100, 1) ELSE 0 END;

        CALL mtnc_cleanup_history_add_message(v_run_id, format('%s: to remove %s out of %s (%s%%)',
                                                               v_root_table, v_deleted, v_total,
                                                               v_percentage));

    ELSE
        -- ACTUAL CLEANUP MODE

        -- Record entities that will be deleted for music-data unbinding
        INSERT INTO mtnc_deleted_entities (cleanup_run_id, entity_type, entity_id)
        SELECT v_run_id, in_entity_type, id
        FROM tmp_entities_to_delete;

        GET DIAGNOSTICS v_deleted = ROW_COUNT;
        CALL mtnc_cleanup_history_add_message(v_run_id, format('recorded %s entities for unbinding in music-data', v_deleted));

        -- 1) attribute_history
        CALL mtnc_cleanup_history_add_message(v_run_id, 'cleanup attribute_history started');

        DELETE FROM attribute_history ah
            USING tmp_entities_to_delete tmp
        WHERE ah.entity_type = in_entity_type
          AND ah.entity_id = tmp.id;

        GET DIAGNOSTICS v_deleted = ROW_COUNT;
        CALL mtnc_cleanup_history_add_message(v_run_id, format('attribute_history: removed %s records', v_deleted));

        DELETE FROM attribute_history ah
            USING tmp_entities_to_delete tmp
        WHERE ah.scope_entity_type = in_entity_type
          AND ah.scope_entity_id = tmp.id;

        GET DIAGNOSTICS v_deleted = ROW_COUNT;
        CALL mtnc_cleanup_history_add_message(v_run_id, format('attribute_history: removed %s records with entity as scope', v_deleted));

        -- 2) api_call (cancel pending calls only)
        CALL mtnc_cleanup_history_add_message(v_run_id, 'cleanup api_call started');

        UPDATE api_call
        SET status = 4  -- cancelled
        FROM tmp_entities_to_delete tmp
        WHERE entity_type = in_entity_type
          AND entity_id = tmp.id
          AND status = 2; -- pending

        GET DIAGNOSTICS v_deleted = ROW_COUNT;
        CALL mtnc_cleanup_history_add_message(v_run_id, format('api_call: cancelled %s pending records', v_deleted));

        -- 3) attribute_snapshot - DO NOT DELETE to preserve data_snapshot integrity
        -- These records will become inaccessible after entity deletion but preserve referential integrity
        CALL mtnc_cleanup_history_add_message(v_run_id, 'attribute_snapshot: preserved for data_snapshot integrity (will become inaccessible)');

        -- 4) data_snapshot - DO NOT DELETE to preserve completed api_call history
        CALL mtnc_cleanup_history_add_message(v_run_id, 'data_snapshot: preserved for api_call history integrity');

        -- 5) Direct foreign key references cleanup (track.artist_id, album.artist_id)
        -- These need to be handled before root table deletion for artists
        IF in_entity_type = 1 THEN -- artist cleanup
            CALL mtnc_cleanup_history_add_message(v_run_id, 'cleanup direct foreign key references started');

            -- Delete tracks that reference artists to be deleted
            DELETE FROM track t
                USING tmp_entities_to_delete tmp
            WHERE t.artist_id = tmp.id;

            GET DIAGNOSTICS v_deleted = ROW_COUNT;
            CALL mtnc_cleanup_history_add_message(v_run_id, format('track: removed %s records (tracks with deleted artist)', v_deleted));

            -- Delete albums that reference artists to be deleted
            DELETE FROM album a
                USING tmp_entities_to_delete tmp
            WHERE a.artist_id = tmp.id;

            GET DIAGNOSTICS v_deleted = ROW_COUNT;
            CALL mtnc_cleanup_history_add_message(v_run_id, format('album: removed %s records (albums with deleted artist)', v_deleted));
        END IF;

        -- 6) Relationship tables cleanup - now automatic due to CASCADE constraints
        CALL mtnc_cleanup_history_add_message(v_run_id, 'relationship tables will be cleaned automatically via CASCADE');

        -- 7) Root table - this will trigger CASCADE cleanup of relationships
        CALL mtnc_cleanup_history_add_message(v_run_id, 'cleanup root table started');

        v_sql := format(
                'DELETE FROM %I WHERE approval_status = 1 AND %I < %s',
                v_root_table, v_threshold_col, in_threshold
                 );
        EXECUTE v_sql;

        GET DIAGNOSTICS v_deleted = ROW_COUNT;
        CALL mtnc_cleanup_history_add_message(v_run_id, format('%s: removed %s records (relationships cleaned via CASCADE)',
                                                               v_root_table, v_deleted));

    END IF;

    -- Cleanup temporary table
    DROP TABLE IF EXISTS tmp_entities_to_delete;

    -- Finalize cleanup run
    UPDATE mtnc_cleanup_run
    SET finish_ts = now()
    WHERE id = v_run_id;

    -- Return cleanup history records
    RETURN QUERY
        SELECT *
        FROM mtnc_cleanup_history
        WHERE cleanup_run_id = v_run_id
        ORDER BY ts;

END;
$$;
