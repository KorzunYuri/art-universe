package yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.processor;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Processor for attribute history staging records.
 * <p>
 * Uses a hot/cold table split for optimal performance:
 * <ul>
 *   <li>attribute_history_current: Hot table for current values (no valid_till column)</li>
 *   <li>attribute_history_archive: Cold table for historical values (append-only)</li>
 * </ul>
 * <p>
 * The key optimization is that instead of UPDATE operations (which require updating
 * all 8 indexes on the original table), we use explicit DELETE + INSERT which is
 * more efficient because:
 * <ol>
 *   <li>Joins start from the small batch and go outward to the current table</li>
 *   <li>The current table is much smaller (only current values)</li>
 *   <li>No partial index maintenance for valid_till filter</li>
 *   <li>Archive table is append-only (no updates)</li>
 * </ol>
 */
@Component
@Slf4j
public class LastfmAttributeHistoryProcessor {

    private static final int BATCH_SIZE = 5000;

    private final LastfmAttributeHistoryProcessor self;
    private final JdbcTemplate jdbcTemplate;

    @Getter
    private volatile String currentStagingTable = "mu_raw_lastfm_staging.stg_attribute_history_a";

    public LastfmAttributeHistoryProcessor(
            JdbcTemplate jdbcTemplate,
            @Lazy LastfmAttributeHistoryProcessor self
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.self = self;
    }

    @Scheduled(
        fixedDelayString = "${lastfm.scheduling.attribute-history.fixed-delay-secs}",
        timeUnit = java.util.concurrent.TimeUnit.SECONDS
    )
    @Transactional
    public void triggerAttributeHistoryProcessing() {
        log.info("start attribute history processing");

        // switch tables
        String processingTable = currentStagingTable;
        currentStagingTable = processingTable.equals("mu_raw_lastfm_staging.stg_attribute_history_a")
            ? "mu_raw_lastfm_staging.stg_attribute_history_b"
            : "mu_raw_lastfm_staging.stg_attribute_history_a";

        log.debug("Switched to writing to {}, processing {}", currentStagingTable, processingTable);

        processStagingRecords(processingTable);
        log.info("finished attribute history processing");
    }

    public void processStagingRecords(String tableName) {
        Long totalCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + tableName, Long.class);
        if (totalCount == null || totalCount == 0) {
            log.debug("No records to process in {}", tableName);
            return;
        }

        log.info("Processing {} records from {} in batches of {}", totalCount, tableName, BATCH_SIZE);

        int totalArchived = 0;
        int totalInserted = 0;
        int batchNumber = 0;
        long offset = 0;

        while (offset < totalCount) {
            batchNumber++;
            final long currentOffset = offset;

            log.info("Processing batch {} (offset: {}, size: {})", batchNumber, offset, BATCH_SIZE);

            // Process batch in a separate transaction
            BatchResult result = self.processBatch(tableName, currentOffset, BATCH_SIZE);

            totalArchived += result.archived();
            totalInserted += result.inserted();

            log.info("Batch {} completed: {} archived, {} inserted (cumulative: {}/{})",
                    batchNumber, result.archived(), result.inserted(), totalArchived, totalInserted);

            offset += BATCH_SIZE;
        }

        log.info("All batches completed: processed {} staging records in {} batches: {} archived, {} inserted",
                totalCount, batchNumber, totalArchived, totalInserted);

        // Truncate staging table after all batches are processed
        jdbcTemplate.execute("TRUNCATE TABLE " + tableName);
        log.info("Truncated staging table: {}", tableName);
    }

    @Transactional
    public BatchResult processBatch(String tableName, long offset, int batchSize) {
        // Drop and recreate temporary table to ensure fresh batch IDs for this transaction
        jdbcTemplate.execute("DROP TABLE IF EXISTS batch_ids");

        String createTempTableSql = String.format("""
            CREATE TEMP TABLE batch_ids AS
            SELECT id FROM %s
            ORDER BY id
            LIMIT %d OFFSET %d
            """, tableName, batchSize, offset);

        jdbcTemplate.execute(createTempTableSql);

        // Add index and analyze for better query planning
        jdbcTemplate.execute("CREATE INDEX ON batch_ids (id)");
        jdbcTemplate.execute("ANALYZE batch_ids");

        // Process records using optimized hot/cold table split:
        // 1. Join from staging (small) to current table (medium) - efficient join direction
        // 2. Archive changed records: DELETE from current + INSERT into archive
        // 3. Insert new/changed values into current table
        //
        // IMPORTANT: CTE execution order is guaranteed by data dependencies:
        // archived -> deleted -> records_to_insert -> inserted
        // This ensures DELETE completes before INSERT to avoid unique constraint violations.
        //
        // Note: Staging table has unique constraint, so duplicates can't exist within a batch.
        String sql = String.format("""
            WITH batch_staging AS (
                -- Get staging records for this batch
                SELECT s.*
                FROM %s s
                INNER JOIN batch_ids b ON s.id = b.id
            ),
            existing_values AS (
                -- Find existing current values for staging records
                -- Join direction: staging (small) -> current (larger) for efficiency
                SELECT
                    c.id as current_id,
                    c.entity_type,
                    c.entity_id,
                    c.attribute_id,
                    c.scope_entity_type,
                    c.scope_entity_id,
                    c.string_value,
                    c.numeric_value,
                    c.collection_ts,
                    c.api_call_id,
                    c.valid_from,
                    s.id as staging_id,
                    s.string_value as new_string_value,
                    s.numeric_value as new_numeric_value,
                    s.valid_from as new_valid_from
                FROM batch_staging s
                INNER JOIN mu_raw_lastfm.attribute_history_current c
                    ON  c.entity_type = s.entity_type
                    AND c.entity_id = s.entity_id
                    AND c.attribute_id = s.attribute_id
                    AND COALESCE(c.scope_entity_type, -1) = COALESCE(s.scope_entity_type, -1)
                    AND COALESCE(c.scope_entity_id, -1) = COALESCE(s.scope_entity_id, -1)
            ),
            changed_records AS (
                -- Identify records that have changed values
                SELECT *
                FROM existing_values
                WHERE string_value IS DISTINCT FROM new_string_value
                   OR numeric_value IS DISTINCT FROM new_numeric_value
            ),
            archived AS (
                -- Move changed records to archive with calculated valid_till
                INSERT INTO mu_raw_lastfm.attribute_history_archive (
                    id, entity_type, entity_id, attribute_id,
                    scope_entity_type, scope_entity_id,
                    string_value, numeric_value,
                    collection_ts, api_call_id,
                    valid_from, valid_till
                )
                SELECT
                    current_id,
                    entity_type,
                    entity_id,
                    attribute_id,
                    scope_entity_type,
                    scope_entity_id,
                    string_value,
                    numeric_value,
                    collection_ts,
                    api_call_id,
                    valid_from,
                    (new_valid_from - INTERVAL '1 day')::date as valid_till
                FROM changed_records
                RETURNING id
            ),
            deleted AS (
                -- Delete archived records from current table
                -- This CTE depends on 'archived', ensuring archive INSERT happens first
                DELETE FROM mu_raw_lastfm.attribute_history_current
                WHERE id IN (SELECT id FROM archived)
                RETURNING id
            ),
            records_to_insert AS (
                -- Records to insert: new records + changed records (after deletion)
                -- References 'deleted' to ensure DELETE completes before INSERT
                SELECT s.*
                FROM batch_staging s
                LEFT JOIN existing_values ev ON ev.staging_id = s.id
                WHERE ev.current_id IS NULL  -- New record (no existing value)
                   OR ev.current_id IN (SELECT id FROM deleted)  -- Changed record (was deleted)
            ),
            inserted AS (
                -- Insert new/updated values into current table
                INSERT INTO mu_raw_lastfm.attribute_history_current (
                    id, entity_type, entity_id, attribute_id,
                    scope_entity_type, scope_entity_id,
                    string_value, numeric_value,
                    collection_ts, api_call_id, valid_from
                )
                SELECT
                    nextval('mu_raw_lastfm.attribute_history_seq'),
                    entity_type,
                    entity_id,
                    attribute_id,
                    scope_entity_type,
                    scope_entity_id,
                    string_value,
                    numeric_value,
                    collection_ts,
                    api_call_id,
                    valid_from
                FROM records_to_insert
                RETURNING id
            )
            SELECT
                (SELECT COUNT(*) FROM archived) as archived,
                (SELECT COUNT(*) FROM inserted) as inserted
            """, tableName);

        BatchResult result = jdbcTemplate.query(sql, rs -> {
            if (rs.next()) {
                return new BatchResult(rs.getInt("archived"), rs.getInt("inserted"));
            }
            return new BatchResult(0, 0);
        });

        // Drop temp table
        jdbcTemplate.execute("DROP TABLE IF EXISTS batch_ids");

        return result != null ? result : new BatchResult(0, 0);
    }

    /**
     * Record to hold batch processing results.
     *
     * @param archived Number of records moved to archive (expired)
     * @param inserted Number of records inserted into current table
     */
    public record BatchResult(int archived, int inserted) {
    }

}
