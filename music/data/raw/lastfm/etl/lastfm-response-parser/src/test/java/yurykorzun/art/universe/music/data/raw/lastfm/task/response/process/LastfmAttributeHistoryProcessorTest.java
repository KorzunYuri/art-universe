package yurykorzun.art.universe.music.data.raw.lastfm.task.response.process;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.attribute.LastfmAttribute;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.common.LastfmEntityType;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.service.attribute.LastfmAttributeHistoryStagingService;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.service.attribute.LastfmAttributeHistoryStagingServiceImpl;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.dto.LastfmAttributeHistoryCandidate;
import yurykorzun.art.universe.music.data.raw.lastfm.test.archetypes.LastfmJpaTestHelper;
import yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.processor.LastfmAttributeHistoryProcessor;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Import({
    LastfmAttributeHistoryProcessor.class,
    LastfmAttributeHistoryStagingServiceImpl.class,
})
class LastfmAttributeHistoryProcessorTest extends LastfmJpaTestHelper {

    @Autowired
    private LastfmAttributeHistoryStagingService attributeHistoryService;

    @Autowired
    private LastfmAttributeHistoryProcessor processor;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        // cleanup staging tables
        jdbcTemplate.execute("TRUNCATE TABLE mu_raw_lastfm_staging.stg_attribute_history_a");
        jdbcTemplate.execute("TRUNCATE TABLE mu_raw_lastfm_staging.stg_attribute_history_b");
        // cleanup main tables (hot/cold split)
        jdbcTemplate.execute("TRUNCATE TABLE mu_raw_lastfm.attribute_history_current");
        jdbcTemplate.execute("TRUNCATE TABLE mu_raw_lastfm.attribute_history_archive");
    }

    @Test
    void shouldInsertIntoCurrentStagingTable() {
        // Given
        var apiCall = consistencyHelper.createAndSaveApiCall();
        var artist = consistencyHelper.createAndSaveArtist();

        var record = LastfmAttributeHistoryCandidate.ofNumeric(
            apiCall.getId(),
            LastfmEntityType.ARTIST,
            artist.getId(),
            LastfmAttribute.LISTENERS_COUNT,
            1000L
        );

        String currentTable = processor.getCurrentStagingTable();

        // When
        attributeHistoryService.upsertCandidateValues(List.of(record));

        // Then
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + currentTable, Integer.class);
        assertEquals(1, count);

        // Verify record content
        var result = jdbcTemplate.queryForMap("SELECT * FROM " + currentTable + " LIMIT 1");
        assertEquals(apiCall.getId(), ((Number) result.get("api_call_id")).longValue());
        assertEquals(LastfmEntityType.ARTIST.getCode(), result.get("entity_type"));
        assertEquals(artist.getId(), ((Number) result.get("entity_id")).longValue());
        assertEquals(LastfmAttribute.LISTENERS_COUNT.getCode(), result.get("attribute_id"));
        assertEquals(1000L, ((Number) result.get("numeric_value")).longValue());
    }

    @Test
    void shouldProcessStagingRecordsAndMergeToMainTable() {
        // Given - create existing record in current table
        var apiCall1 = consistencyHelper.createAndSaveApiCall();
        var artist = consistencyHelper.createAndSaveArtist();
        entityManager.flush();

        var existingRecord = LastfmAttributeHistoryCandidate.ofNumeric(
            apiCall1.getId(),
            LastfmEntityType.ARTIST,
            artist.getId(),
            LastfmAttribute.LISTENERS_COUNT,
            500L
        );

        // Insert into attribute_history_current (no valid_till column)
        jdbcTemplate.update("""
            INSERT INTO mu_raw_lastfm.attribute_history_current
            (id, api_call_id, entity_type, entity_id, attribute_id, numeric_value, valid_from, collection_ts)
            VALUES (nextval('mu_raw_lastfm.attribute_history_seq'), ?, ?, ?, ?, ?, ?, NOW())
            """,
            existingRecord.apiCallId(),
            existingRecord.entityType().getCode(),
            existingRecord.entityId(),
            existingRecord.attribute().getCode(),
            existingRecord.numericValue(),
            LocalDate.now().minusDays(1)
        );

        // Given - add new record with changed value to staging
        var apiCall2 = consistencyHelper.createAndSaveApiCall();
        var newRecord = LastfmAttributeHistoryCandidate.ofNumeric(
            apiCall2.getId(),
            LastfmEntityType.ARTIST,
            artist.getId(),
            LastfmAttribute.LISTENERS_COUNT,
            1000L
        );

        String currentTable = processor.getCurrentStagingTable();
        jdbcTemplate.update("""
            INSERT INTO %s
            (api_call_id, entity_type, entity_id, attribute_id, numeric_value, valid_from, collection_ts)
            VALUES (?, ?, ?, ?, ?, ?, NOW())
            """.formatted(currentTable),
            newRecord.apiCallId(),
            newRecord.entityType().getCode(),
            newRecord.entityId(),
            newRecord.attribute().getCode(),
            newRecord.numericValue(),
            newRecord.validFrom()
        );
        entityManager.flush();

        // When
        processor.processStagingRecords(currentTable);

        // Then - verify old record was archived
        Integer archivedCount = jdbcTemplate.queryForObject("""
            SELECT COUNT(*) FROM mu_raw_lastfm.attribute_history_archive
            WHERE entity_id = ? AND attribute_id = ?
            """, Integer.class, artist.getId(), LastfmAttribute.LISTENERS_COUNT.getCode());
        assertEquals(1, archivedCount);

        // Then - verify new record is in current table
        Integer currentCount = jdbcTemplate.queryForObject("""
            SELECT COUNT(*) FROM mu_raw_lastfm.attribute_history_current
            WHERE entity_id = ? AND attribute_id = ?
            """, Integer.class, artist.getId(), LastfmAttribute.LISTENERS_COUNT.getCode());
        assertEquals(1, currentCount);

        // Then - verify value of new record
        Long currentValue = jdbcTemplate.queryForObject("""
            SELECT numeric_value FROM mu_raw_lastfm.attribute_history_current
            WHERE entity_id = ? AND attribute_id = ?
            """, Long.class, artist.getId(), LastfmAttribute.LISTENERS_COUNT.getCode());
        assertEquals(1000L, currentValue);

        // Then - staging table should be truncated
        Integer stagingCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + currentTable, Integer.class);
        assertEquals(0, stagingCount);
    }

    @Test
    void shouldNotProcessUnchangedValues() {
        // Given
        var apiCall1 = consistencyHelper.createAndSaveApiCall();
        var artist = consistencyHelper.createAndSaveArtist();
        entityManager.flush();

        // Insert existing record into current table
        jdbcTemplate.update("""
            INSERT INTO mu_raw_lastfm.attribute_history_current
            (id, api_call_id, entity_type, entity_id, attribute_id, numeric_value, valid_from, collection_ts)
            VALUES (nextval('mu_raw_lastfm.attribute_history_seq'), ?, ?, ?, ?, ?, ?, NOW())
            """,
            apiCall1.getId(),
            LastfmEntityType.ARTIST.getCode(),
            artist.getId(),
            LastfmAttribute.LISTENERS_COUNT.getCode(),
            1000L,
            LocalDate.now()
        );

        // Given - add staging record with same value
        var apiCall2 = consistencyHelper.createAndSaveApiCall();
        String currentTable = processor.getCurrentStagingTable();
        jdbcTemplate.update("""
            INSERT INTO %s
            (api_call_id, entity_type, entity_id, attribute_id, numeric_value, valid_from, collection_ts)
            VALUES (?, ?, ?, ?, ?, ?, NOW())
            """.formatted(currentTable),
            apiCall2.getId(),
            LastfmEntityType.ARTIST.getCode(),
            artist.getId(),
            LastfmAttribute.LISTENERS_COUNT.getCode(),
            1000L, // same value
            LocalDate.now()
        );
        entityManager.flush();

        // When
        processor.processStagingRecords(currentTable);

        // Then - should still have only one record in current (not archived)
        Integer currentCount = jdbcTemplate.queryForObject("""
            SELECT COUNT(*) FROM mu_raw_lastfm.attribute_history_current
            WHERE entity_id = ? AND attribute_id = ?
            """, Integer.class, artist.getId(), LastfmAttribute.LISTENERS_COUNT.getCode());
        assertEquals(1, currentCount);

        // Then - no records should be archived
        Integer archivedCount = jdbcTemplate.queryForObject("""
            SELECT COUNT(*) FROM mu_raw_lastfm.attribute_history_archive
            WHERE entity_id = ? AND attribute_id = ?
            """, Integer.class, artist.getId(), LastfmAttribute.LISTENERS_COUNT.getCode());
        assertEquals(0, archivedCount);
    }

    @Test
    void shouldDeduplicateRecordsInStagingTable() {
        // Given
        var apiCall1 = consistencyHelper.createAndSaveApiCall();
        var apiCall2 = consistencyHelper.createAndSaveApiCall();
        var artist = consistencyHelper.createAndSaveArtist();
        entityManager.flush();

        var record1 = LastfmAttributeHistoryCandidate.ofNumeric(
            apiCall1.getId(),
            LastfmEntityType.ARTIST,
            artist.getId(),
            LastfmAttribute.LISTENERS_COUNT,
            1000L
        );

        var record2 = LastfmAttributeHistoryCandidate.ofNumeric(
            apiCall2.getId(),
            LastfmEntityType.ARTIST,
            artist.getId(),
            LastfmAttribute.LISTENERS_COUNT,
            2000L // different value
        );

        // When - insert first record
        attributeHistoryService.upsertCandidateValues(List.of(record1));

        String currentTable = processor.getCurrentStagingTable();
        Integer countAfterFirst = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + currentTable, Integer.class);
        assertEquals(1, countAfterFirst);

        // When - insert second record with same key but different value
        attributeHistoryService.upsertCandidateValues(List.of(record2));

        // Then - should still have only one record (deduplicated)
        Integer countAfterSecond = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + currentTable, Integer.class);
        assertEquals(1, countAfterSecond);

        // Then - should have the latest value
        Long actualValue = jdbcTemplate.queryForObject(
            "SELECT numeric_value FROM " + currentTable + " WHERE entity_id = ? AND attribute_id = ?",
            Long.class, artist.getId(), LastfmAttribute.LISTENERS_COUNT.getCode());
        assertEquals(2000L, actualValue);

        // Then - should have the latest api_call_id
        Long actualApiCallId = jdbcTemplate.queryForObject(
            "SELECT api_call_id FROM " + currentTable + " WHERE entity_id = ? AND attribute_id = ?",
            Long.class, artist.getId(), LastfmAttribute.LISTENERS_COUNT.getCode());
        assertEquals(apiCall2.getId(), actualApiCallId);
    }

    @Test
    void shouldSwitchBetweenStagingTables() {
        // Given
        String initialTable = processor.getCurrentStagingTable();

        // When
        processor.processStagingRecords(initialTable);

        // Manually switch tables to simulate the behavior
        String expectedNewTable = initialTable.equals("mu_raw_lastfm_staging.attribute_history_staging_a")
            ? "mu_raw_lastfm_staging.attribute_history_staging_b"
            : "mu_raw_lastfm_staging.attribute_history_staging_a";

        // Then - check that tables are different
        assertNotEquals(initialTable, expectedNewTable);
        assertTrue(expectedNewTable.contains("staging_a") || expectedNewTable.contains("staging_b"));
    }

    @Test
    void shouldProcessMultipleBatchesCorrectly() {
        // Given - create 7 records for different artists to be processed in multiple batches (3+3+1 with batch size 3)
        String currentTable = processor.getCurrentStagingTable();
        var artistIds = new java.util.ArrayList<Long>();

        // Insert 7 staging records with different artists (to avoid unique constraint violations)
        for (int i = 0; i < 7; i++) {
            var apiCall = consistencyHelper.createAndSaveApiCall();
            var artist = consistencyHelper.createAndSaveArtist();
            artistIds.add(artist.getId());

            jdbcTemplate.update("""
                INSERT INTO %s
                (api_call_id, entity_type, entity_id, attribute_id, numeric_value, valid_from, collection_ts)
                VALUES (?, ?, ?, ?, ?, ?, NOW())
                """.formatted(currentTable),
                apiCall.getId(),
                LastfmEntityType.ARTIST.getCode(),
                artist.getId(),
                LastfmAttribute.LISTENERS_COUNT.getCode(),
                (i + 1) * 1000L, // 1000, 2000, 3000, ..., 7000
                LocalDate.now()
            );
        }
        entityManager.flush();

        // When - process in batches of 3
        int batchSize = 3;
        int totalArchived = 0;
        int totalInserted = 0;
        long offset = 0;
        long totalRecords = 7;

        while (offset < totalRecords) {
            var result = processor.processBatch(currentTable, offset, batchSize);
            totalArchived += result.archived();
            totalInserted += result.inserted();
            offset += batchSize;
        }

        // Then - all 7 records should be inserted (no existing records, so no archives)
        assertEquals(0, totalArchived, "Should archive 0 records (all new)");
        assertEquals(7, totalInserted, "Should insert all 7 records");

        // Verify all 7 artists have their records in current table
        for (int i = 0; i < 7; i++) {
            Long artistId = artistIds.get(i);
            Long value = jdbcTemplate.queryForObject("""
                SELECT numeric_value FROM mu_raw_lastfm.attribute_history_current
                WHERE entity_id = ? AND attribute_id = ?
                """, Long.class, artistId, LastfmAttribute.LISTENERS_COUNT.getCode());
            assertEquals((i + 1) * 1000L, value, String.format("Artist %d should have correct value", i));
        }

        // Verify total count in current table
        Integer totalCount = jdbcTemplate.queryForObject("""
            SELECT COUNT(*) FROM mu_raw_lastfm.attribute_history_current
            """, Integer.class);
        assertTrue(totalCount >= 7, "Should have at least 7 current records");
    }

    @Test
    void shouldHandlePartialLastBatch() {
        // Given - create 5 records to test partial last batch (3+2 with batch size 3)
        String currentTable = processor.getCurrentStagingTable();

        for (int i = 0; i < 5; i++) {
            var apiCall = consistencyHelper.createAndSaveApiCall();
            var artist = consistencyHelper.createAndSaveArtist();
            jdbcTemplate.update("""
                INSERT INTO %s
                (api_call_id, entity_type, entity_id, attribute_id, numeric_value, valid_from, collection_ts)
                VALUES (?, ?, ?, ?, ?, ?, NOW())
                """.formatted(currentTable),
                apiCall.getId(),
                LastfmEntityType.ARTIST.getCode(),
                artist.getId(),
                LastfmAttribute.LISTENERS_COUNT.getCode(),
                1000L,
                LocalDate.now()
            );
        }
        entityManager.flush();

        // When - process in batches of 3 (should process 3 + 2)
        int batchSize = 3;
        int totalInserted = 0;
        long offset = 0;
        long totalRecords = 5;

        while (offset < totalRecords) {
            var result = processor.processBatch(currentTable, offset, batchSize);
            totalInserted += result.inserted();
            offset += batchSize;
        }

        // Then - all 5 records should be inserted
        assertEquals(5, totalInserted, "All 5 records should be inserted including partial last batch");

        // Verify 5 records in current table
        Integer count = jdbcTemplate.queryForObject("""
            SELECT COUNT(*) FROM mu_raw_lastfm.attribute_history_current
            """, Integer.class);
        assertEquals(5, count, "Should have 5 current records in current table");
    }

    @Test
    void shouldHandleSameEntityAcrossMultipleBatches() {
        // Given - create existing records in current table, then create staging updates across multiple batches
        String currentTable = processor.getCurrentStagingTable();
        var apiCall1 = consistencyHelper.createAndSaveApiCall();
        entityManager.flush();

        // Create 6 artists with existing values in current table
        var artistIds = new java.util.ArrayList<Long>();
        for (int i = 0; i < 6; i++) {
            var artist = consistencyHelper.createAndSaveArtist();
            artistIds.add(artist.getId());
            entityManager.flush();

            // Insert existing record in current table (no valid_till column)
            jdbcTemplate.update("""
                INSERT INTO mu_raw_lastfm.attribute_history_current
                (id, api_call_id, entity_type, entity_id, attribute_id, numeric_value, valid_from, collection_ts)
                VALUES (nextval('mu_raw_lastfm.attribute_history_seq'), ?, ?, ?, ?, ?, ?, NOW())
                """,
                apiCall1.getId(),
                LastfmEntityType.ARTIST.getCode(),
                artist.getId(),
                LastfmAttribute.LISTENERS_COUNT.getCode(),
                (i + 1) * 100L, // 100, 200, ..., 600
                LocalDate.now().minusDays(1)
            );

            // Insert staging record with updated value
            var apiCall2 = consistencyHelper.createAndSaveApiCall();
            entityManager.flush();

            jdbcTemplate.update("""
                INSERT INTO %s
                (api_call_id, entity_type, entity_id, attribute_id, numeric_value, valid_from, collection_ts)
                VALUES (?, ?, ?, ?, ?, ?, NOW())
                """.formatted(currentTable),
                apiCall2.getId(),
                LastfmEntityType.ARTIST.getCode(),
                artist.getId(),
                LastfmAttribute.LISTENERS_COUNT.getCode(),
                (i + 1) * 1000L, // 1000, 2000, ..., 6000 (updated values)
                LocalDate.now()
            );
        }

        // When - process in 2 batches of 3
        int batchSize = 3;
        int totalArchived = 0;
        int totalInserted = 0;

        // Process first batch (records 1-3)
        var result1 = processor.processBatch(currentTable, 0, batchSize);
        totalArchived += result1.archived();
        totalInserted += result1.inserted();

        // Ensure temp table is dropped between batches (in test context)
        jdbcTemplate.execute("DROP TABLE IF EXISTS batch_ids");

        // Process second batch (records 4-6)
        var result2 = processor.processBatch(currentTable, batchSize, batchSize);
        totalArchived += result2.archived();
        totalInserted += result2.inserted();

        // Then - verify cumulative results: all 6 old records should be archived, 6 new inserted
        assertEquals(6, totalArchived, "Should archive all 6 existing records");
        assertEquals(6, totalInserted, "Should insert all 6 updated records");

        // Verify each artist has the updated value in current table
        for (int i = 0; i < 6; i++) {
            Long artistId = artistIds.get(i);
            Long currentValue = jdbcTemplate.queryForObject("""
                SELECT numeric_value FROM mu_raw_lastfm.attribute_history_current
                WHERE entity_id = ? AND attribute_id = ?
                """, Long.class, artistId, LastfmAttribute.LISTENERS_COUNT.getCode());
            assertEquals((i + 1) * 1000L, currentValue,
                String.format("Artist %d should have updated value", i));

            // Verify old value is archived
            Integer archivedCount = jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM mu_raw_lastfm.attribute_history_archive
                WHERE entity_id = ? AND attribute_id = ?
                """, Integer.class, artistId, LastfmAttribute.LISTENERS_COUNT.getCode());
            assertEquals(1, archivedCount, String.format("Artist %d should have 1 archived record", i));
        }
    }

    @Test
    void shouldHandleBatchBoundariesCorrectly() {
        // Given - create exactly 6 records (2 batches of 3) with different entities
        // This tests that records at batch boundaries are processed correctly
        String currentTable = processor.getCurrentStagingTable();

        var artists = new java.util.ArrayList<Long>();
        for (int i = 0; i < 6; i++) {
            var apiCall = consistencyHelper.createAndSaveApiCall();
            var artist = consistencyHelper.createAndSaveArtist();
            artists.add(artist.getId());

            jdbcTemplate.update("""
                INSERT INTO %s
                (api_call_id, entity_type, entity_id, attribute_id, numeric_value, valid_from, collection_ts)
                VALUES (?, ?, ?, ?, ?, ?, NOW())
                """.formatted(currentTable),
                apiCall.getId(),
                LastfmEntityType.ARTIST.getCode(),
                artist.getId(),
                LastfmAttribute.LISTENERS_COUNT.getCode(),
                (i + 1) * 1000L,
                LocalDate.now()
            );
        }
        entityManager.flush();

        // When - process in 2 batches of exactly 3
        processor.processBatch(currentTable, 0, 3);
        processor.processBatch(currentTable, 3, 3);

        // Then - verify all 6 artists have their records in current table
        for (int i = 0; i < 6; i++) {
            Long artistId = artists.get(i);
            Long value = jdbcTemplate.queryForObject("""
                SELECT numeric_value FROM mu_raw_lastfm.attribute_history_current
                WHERE entity_id = ? AND attribute_id = ?
                """, Long.class, artistId, LastfmAttribute.LISTENERS_COUNT.getCode());

            assertEquals((i + 1) * 1000L, value,
                String.format("Artist %d should have value %d", i, (i + 1) * 1000));
        }

        // Verify total count in current table
        Integer totalCount = jdbcTemplate.queryForObject("""
            SELECT COUNT(*) FROM mu_raw_lastfm.attribute_history_current
            """, Integer.class);
        assertEquals(6, totalCount, "Should have exactly 6 current records");
    }

    @Test
    void shouldHandleMixedOperationsInBatch() {
        // Given - create some existing records and some new records in staging
        String currentTable = processor.getCurrentStagingTable();

        // Create 2 existing records in current table
        var artist1 = consistencyHelper.createAndSaveArtist();
        var artist2 = consistencyHelper.createAndSaveArtist();
        var apiCall1 = consistencyHelper.createAndSaveApiCall();
        entityManager.flush();

        jdbcTemplate.update("""
            INSERT INTO mu_raw_lastfm.attribute_history_current
            (id, api_call_id, entity_type, entity_id, attribute_id, numeric_value, valid_from, collection_ts)
            VALUES (nextval('mu_raw_lastfm.attribute_history_seq'), ?, ?, ?, ?, ?, ?, NOW())
            """,
            apiCall1.getId(),
            LastfmEntityType.ARTIST.getCode(),
            artist1.getId(),
            LastfmAttribute.LISTENERS_COUNT.getCode(),
            500L,
            LocalDate.now().minusDays(1)
        );

        jdbcTemplate.update("""
            INSERT INTO mu_raw_lastfm.attribute_history_current
            (id, api_call_id, entity_type, entity_id, attribute_id, numeric_value, valid_from, collection_ts)
            VALUES (nextval('mu_raw_lastfm.attribute_history_seq'), ?, ?, ?, ?, ?, ?, NOW())
            """,
            apiCall1.getId(),
            LastfmEntityType.ARTIST.getCode(),
            artist2.getId(),
            LastfmAttribute.LISTENERS_COUNT.getCode(),
            600L,
            LocalDate.now().minusDays(1)
        );

        // Create staging records: 2 updates + 2 new inserts
        // Updates for artist1 and artist2
        var apiCall2 = consistencyHelper.createAndSaveApiCall();
        entityManager.flush();

        jdbcTemplate.update("""
            INSERT INTO %s
            (api_call_id, entity_type, entity_id, attribute_id, numeric_value, valid_from, collection_ts)
            VALUES (?, ?, ?, ?, ?, ?, NOW())
            """.formatted(currentTable),
            apiCall2.getId(),
            LastfmEntityType.ARTIST.getCode(),
            artist1.getId(),
            LastfmAttribute.LISTENERS_COUNT.getCode(),
            1500L, // updated value
            LocalDate.now()
        );

        jdbcTemplate.update("""
            INSERT INTO %s
            (api_call_id, entity_type, entity_id, attribute_id, numeric_value, valid_from, collection_ts)
            VALUES (?, ?, ?, ?, ?, ?, NOW())
            """.formatted(currentTable),
            apiCall2.getId(),
            LastfmEntityType.ARTIST.getCode(),
            artist2.getId(),
            LastfmAttribute.LISTENERS_COUNT.getCode(),
            1600L, // updated value
            LocalDate.now()
        );

        // New inserts for artist3 and artist4
        var artist3 = consistencyHelper.createAndSaveArtist();
        var artist4 = consistencyHelper.createAndSaveArtist();
        entityManager.flush();

        jdbcTemplate.update("""
            INSERT INTO %s
            (api_call_id, entity_type, entity_id, attribute_id, numeric_value, valid_from, collection_ts)
            VALUES (?, ?, ?, ?, ?, ?, NOW())
            """.formatted(currentTable),
            apiCall2.getId(),
            LastfmEntityType.ARTIST.getCode(),
            artist3.getId(),
            LastfmAttribute.LISTENERS_COUNT.getCode(),
            2000L,
            LocalDate.now()
        );

        jdbcTemplate.update("""
            INSERT INTO %s
            (api_call_id, entity_type, entity_id, attribute_id, numeric_value, valid_from, collection_ts)
            VALUES (?, ?, ?, ?, ?, ?, NOW())
            """.formatted(currentTable),
            apiCall2.getId(),
            LastfmEntityType.ARTIST.getCode(),
            artist4.getId(),
            LastfmAttribute.LISTENERS_COUNT.getCode(),
            3000L,
            LocalDate.now()
        );

        // When - process batch
        var result = processor.processBatch(currentTable, 0, 10);

        // Then - should have 2 archived (artist1 and artist2 old values) and 4 inserted (2 updates + 2 new)
        assertEquals(2, result.archived(), "Should archive 2 existing records");
        assertEquals(4, result.inserted(), "Should insert 4 records (2 updates + 2 new)");

        // Verify updated values in current table
        Long artist1Value = jdbcTemplate.queryForObject("""
            SELECT numeric_value FROM mu_raw_lastfm.attribute_history_current
            WHERE entity_id = ? AND attribute_id = ?
            """, Long.class, artist1.getId(), LastfmAttribute.LISTENERS_COUNT.getCode());
        assertEquals(1500L, artist1Value);

        Long artist2Value = jdbcTemplate.queryForObject("""
            SELECT numeric_value FROM mu_raw_lastfm.attribute_history_current
            WHERE entity_id = ? AND attribute_id = ?
            """, Long.class, artist2.getId(), LastfmAttribute.LISTENERS_COUNT.getCode());
        assertEquals(1600L, artist2Value);

        // Verify new values in current table
        Long artist3Value = jdbcTemplate.queryForObject("""
            SELECT numeric_value FROM mu_raw_lastfm.attribute_history_current
            WHERE entity_id = ? AND attribute_id = ?
            """, Long.class, artist3.getId(), LastfmAttribute.LISTENERS_COUNT.getCode());
        assertEquals(2000L, artist3Value);

        Long artist4Value = jdbcTemplate.queryForObject("""
            SELECT numeric_value FROM mu_raw_lastfm.attribute_history_current
            WHERE entity_id = ? AND attribute_id = ?
            """, Long.class, artist4.getId(), LastfmAttribute.LISTENERS_COUNT.getCode());
        assertEquals(3000L, artist4Value);

        // Verify archived records
        Integer archivedCount = jdbcTemplate.queryForObject("""
            SELECT COUNT(*) FROM mu_raw_lastfm.attribute_history_archive
            """, Integer.class);
        assertEquals(2, archivedCount, "Should have 2 archived records");
    }

    @Test
    void shouldTakeLatestRecordWhenMultipleStagingRecordsForSameEntityAttribute() {
        // Given - manually insert multiple staging records for the same entity/attribute
        // with different valid_from dates to simulate race condition or edge case
        String currentTable = processor.getCurrentStagingTable();

        var apiCall1 = consistencyHelper.createAndSaveApiCall();
        var apiCall2 = consistencyHelper.createAndSaveApiCall();
        var apiCall3 = consistencyHelper.createAndSaveApiCall();
        var artist = consistencyHelper.createAndSaveArtist();
        entityManager.flush();

        // Insert 3 records for the same entity/attribute with different dates
        // Oldest record
        jdbcTemplate.update("""
            INSERT INTO %s
            (api_call_id, entity_type, entity_id, attribute_id, numeric_value, valid_from, collection_ts)
            VALUES (?, ?, ?, ?, ?, ?, NOW() - INTERVAL '2 hours')
            """.formatted(currentTable),
            apiCall1.getId(),
            LastfmEntityType.ARTIST.getCode(),
            artist.getId(),
            LastfmAttribute.LISTENERS_COUNT.getCode(),
            1000L, // oldest value
            LocalDate.now().minusDays(2)
        );

        // Middle record
        jdbcTemplate.update("""
            INSERT INTO %s
            (api_call_id, entity_type, entity_id, attribute_id, numeric_value, valid_from, collection_ts)
            VALUES (?, ?, ?, ?, ?, ?, NOW() - INTERVAL '1 hour')
            """.formatted(currentTable),
            apiCall2.getId(),
            LastfmEntityType.ARTIST.getCode(),
            artist.getId(),
            LastfmAttribute.LISTENERS_COUNT.getCode(),
            2000L, // middle value
            LocalDate.now().minusDays(1)
        );

        // Latest record (should be selected)
        jdbcTemplate.update("""
            INSERT INTO %s
            (api_call_id, entity_type, entity_id, attribute_id, numeric_value, valid_from, collection_ts)
            VALUES (?, ?, ?, ?, ?, ?, NOW())
            """.formatted(currentTable),
            apiCall3.getId(),
            LastfmEntityType.ARTIST.getCode(),
            artist.getId(),
            LastfmAttribute.LISTENERS_COUNT.getCode(),
            3000L, // latest value - should be used
            LocalDate.now()
        );

        // Verify we have 3 records in staging
        Integer stagingCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + currentTable, Integer.class);
        assertEquals(3, stagingCount, "Should have 3 staging records before processing");

        // When
        var result = processor.processBatch(currentTable, 0, 10);

        // Then - should only insert 1 record (the latest one, deduplicated)
        assertEquals(0, result.archived(), "Should archive 0 records (no existing values)");
        assertEquals(1, result.inserted(), "Should insert only 1 record (deduplicated from 3)");

        // Verify the inserted value is the latest one
        Long currentValue = jdbcTemplate.queryForObject("""
            SELECT numeric_value FROM mu_raw_lastfm.attribute_history_current
            WHERE entity_id = ? AND attribute_id = ?
            """, Long.class, artist.getId(), LastfmAttribute.LISTENERS_COUNT.getCode());
        assertEquals(3000L, currentValue, "Should have the latest value (3000L)");

        // Verify only 1 record in current table for this entity/attribute
        Integer currentCount = jdbcTemplate.queryForObject("""
            SELECT COUNT(*) FROM mu_raw_lastfm.attribute_history_current
            WHERE entity_id = ? AND attribute_id = ?
            """, Integer.class, artist.getId(), LastfmAttribute.LISTENERS_COUNT.getCode());
        assertEquals(1, currentCount, "Should have exactly 1 current record");
    }

    @Test
    void shouldTakeLatestByCollectionTsWhenValidFromIsSame() {
        // Given - multiple staging records with same valid_from but different collection_ts
        String currentTable = processor.getCurrentStagingTable();

        var apiCall1 = consistencyHelper.createAndSaveApiCall();
        var apiCall2 = consistencyHelper.createAndSaveApiCall();
        var artist = consistencyHelper.createAndSaveArtist();
        entityManager.flush();

        // Earlier collection timestamp
        jdbcTemplate.update("""
            INSERT INTO %s
            (api_call_id, entity_type, entity_id, attribute_id, numeric_value, valid_from, collection_ts)
            VALUES (?, ?, ?, ?, ?, ?, NOW() - INTERVAL '1 hour')
            """.formatted(currentTable),
            apiCall1.getId(),
            LastfmEntityType.ARTIST.getCode(),
            artist.getId(),
            LastfmAttribute.LISTENERS_COUNT.getCode(),
            1000L,
            LocalDate.now() // same valid_from
        );

        // Later collection timestamp (should be selected)
        jdbcTemplate.update("""
            INSERT INTO %s
            (api_call_id, entity_type, entity_id, attribute_id, numeric_value, valid_from, collection_ts)
            VALUES (?, ?, ?, ?, ?, ?, NOW())
            """.formatted(currentTable),
            apiCall2.getId(),
            LastfmEntityType.ARTIST.getCode(),
            artist.getId(),
            LastfmAttribute.LISTENERS_COUNT.getCode(),
            2000L, // this should be used
            LocalDate.now() // same valid_from
        );

        // When
        var result = processor.processBatch(currentTable, 0, 10);

        // Then - should insert only 1 record with the latest collection timestamp
        assertEquals(1, result.inserted(), "Should insert only 1 record");

        Long currentValue = jdbcTemplate.queryForObject("""
            SELECT numeric_value FROM mu_raw_lastfm.attribute_history_current
            WHERE entity_id = ? AND attribute_id = ?
            """, Long.class, artist.getId(), LastfmAttribute.LISTENERS_COUNT.getCode());
        assertEquals(2000L, currentValue, "Should have the value from latest collection_ts (2000L)");

        Long currentApiCallId = jdbcTemplate.queryForObject("""
            SELECT api_call_id FROM mu_raw_lastfm.attribute_history_current
            WHERE entity_id = ? AND attribute_id = ?
            """, Long.class, artist.getId(), LastfmAttribute.LISTENERS_COUNT.getCode());
        assertEquals(apiCall2.getId(), currentApiCallId, "Should have api_call_id from latest collection_ts");
    }
}
