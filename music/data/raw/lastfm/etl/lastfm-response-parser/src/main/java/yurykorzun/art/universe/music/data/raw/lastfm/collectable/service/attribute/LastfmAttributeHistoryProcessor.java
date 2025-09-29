package yurykorzun.art.universe.music.data.raw.lastfm.collectable.service.attribute;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import yurykorzun.art.universe.music.data.raw.lastfm.maintenance.service.TaskCoordinator;

@Component
@Slf4j
public class LastfmAttributeHistoryProcessor {

    public static final String TASK_NAME_ATTRIBUTE_HISTORY_PROCESSING = "attribute-history-processing";

    private final LastfmAttributeHistoryProcessor self;
    private final TaskCoordinator coordinator;
    private final JdbcTemplate jdbcTemplate;
    @Getter
    private volatile String currentStagingTable = "mu_raw_lastfm_staging.stg_attribute_history_a";

    public LastfmAttributeHistoryProcessor(
            TaskCoordinator coordinator,
            JdbcTemplate jdbcTemplate,
            @Lazy LastfmAttributeHistoryProcessor self
    ) {
        this.coordinator = coordinator;
        this.jdbcTemplate = jdbcTemplate;
        this.self = self;
    }

    @Scheduled(fixedDelayString = "${lastfm.scheduling.attribute-history.fixedDelaySecs:30}", timeUnit = java.util.concurrent.TimeUnit.SECONDS)
    public void triggerAttributeHistoryProcessing() {
        coordinator.executeIfAllowed(() -> {
            log.info("start attribute history processing");
            
            // switch tables
            String processingTable = currentStagingTable;
            currentStagingTable = processingTable.equals("mu_raw_lastfm_staging.stg_attribute_history_a") 
                ? "mu_raw_lastfm_staging.stg_attribute_history_b" 
                : "mu_raw_lastfm_staging.stg_attribute_history_a";
                
            log.debug("Switched to writing to {}, processing {}", currentStagingTable, processingTable);
            
            self.processStagingRecords(processingTable);
            log.info("finished attribute history processing");
        }, TASK_NAME_ATTRIBUTE_HISTORY_PROCESSING);
    }

    @Transactional
    public void processStagingRecords(String tableName) {
        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + tableName, Long.class);
        if (count == null || count == 0) {
            log.debug("No records to process in {}", tableName);
            return;
        }

        log.debug("Processing {} records from {}", count, tableName);

        // Expire old records and insert new ones in one transaction
        String sql = String.format("""
            WITH existing_values AS (
                SELECT
                        ah.entity_type
                    ,   ah.entity_id
                    ,   ah.attribute_id
                    ,   ah.scope_entity_type
                    ,   ah.scope_entity_id
                    ,   ah.string_value
                    ,   ah.numeric_value
                    ,   ah.id as existing_id
                FROM
                    mu_raw_lastfm.attribute_history ah
                WHERE   ah.valid_till = '9999-12-31'
                    AND EXISTS (
                        SELECT 1 FROM %s s
                        WHERE   s.entity_type   = ah.entity_type
                            AND s.entity_id     = ah.entity_id
                            AND s.attribute_id  = ah.attribute_id
                            AND COALESCE(s.scope_entity_type, -1)   = COALESCE(ah.scope_entity_type, -1)
                            AND COALESCE(s.scope_entity_id, -1)     = COALESCE(ah.scope_entity_id, -1)
                  )
            ),
            changed_records AS (
                SELECT
                        s.*
                    ,   ev.existing_id
                FROM
                    %s s
                LEFT JOIN
                    existing_values ev
                        ON      s.entity_type = ev.entity_type
                            AND s.entity_id = ev.entity_id
                            AND s.attribute_id = ev.attribute_id
                            AND COALESCE(s.scope_entity_type, -1) = COALESCE(ev.scope_entity_type, -1)
                            AND COALESCE(s.scope_entity_id, -1) = COALESCE(ev.scope_entity_id, -1)
                WHERE   ev.existing_id  IS NULL
                    OR ev.string_value  IS DISTINCT FROM s.string_value
                    OR ev.numeric_value IS DISTINCT FROM s.numeric_value
            ),
            expired_count AS (
                UPDATE mu_raw_lastfm.attribute_history
                SET     valid_till = (SELECT (valid_from - INTERVAL '1 day')::date FROM changed_records cr WHERE cr.existing_id = mu_raw_lastfm.attribute_history.id)
                WHERE id IN (SELECT existing_id FROM changed_records WHERE existing_id IS NOT NULL)
                RETURNING 1
            ),
            inserted_count AS (
                INSERT INTO mu_raw_lastfm.attribute_history (
                    id, api_call_id, entity_type, entity_id, attribute_id,
                    scope_entity_type, scope_entity_id,
                    string_value, numeric_value,
                    collection_ts, valid_from, valid_till)
                SELECT nextval('mu_raw_lastfm.attribute_history_seq'), api_call_id, entity_type, entity_id, attribute_id,
                       scope_entity_type, scope_entity_id,
                       string_value, numeric_value,
                       collection_ts, valid_from, '9999-12-31'::date
                FROM changed_records
                RETURNING 1
            )
            SELECT
                (SELECT COUNT(*) FROM expired_count) as expired,
                (SELECT COUNT(*) FROM inserted_count) as inserted
            """, tableName, tableName);

        jdbcTemplate.query(sql, rs -> {
            int expired = rs.getInt("expired");
            int inserted = rs.getInt("inserted");
            log.info("Processed {} staging records: {} expired, {} inserted", count, expired, inserted);
        });

        jdbcTemplate.execute("TRUNCATE TABLE " + tableName);
    }

}
