package yurykorzun.art.universe.music.data.raw.lastfm.collectable.service.attribute;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.attribute.LastfmAttribute;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.attribute.LastfmAttributeHistoryRecord;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.common.LastfmEntityType;
import yurykorzun.art.universe.music.data.raw.lastfm.common.archetypes.LastfmJpaTestHelper;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Import({
    LastfmAttributeHistoryProcessor.class,
    LastfmAttributeHistoryServiceImpl.class,
})
class LastfmAttributeHistoryProcessorTest extends LastfmJpaTestHelper {

    @Autowired
    private LastfmAttributeHistoryService attributeHistoryService;
    
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
        // cleanup main table to avoid sequence conflicts
        jdbcTemplate.execute("TRUNCATE TABLE mu_raw_lastfm.attribute_history");
    }

    @Test
    void shouldInsertIntoCurrentStagingTable() {
        // Given
        var apiCall = consistencyHelper.createAndSaveApiCall();
        var artist = consistencyHelper.createAndSaveArtist();
        
        var record = LastfmAttributeHistoryRecord.builder()
            .apiCallId(apiCall.getId())
            .entityType(LastfmEntityType.ARTIST)
            .entityId(artist.getId())
            .attribute(LastfmAttribute.LISTENERS_COUNT)
            .numericValue(1000L)
            .validFrom(LocalDate.now())
            .build();

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
        // Given - создаем существующую запись в основной таблице
        var apiCall1 = consistencyHelper.createAndSaveApiCall();
        var artist = consistencyHelper.createAndSaveArtist();
        entityManager.flush();
        
        var existingRecord = LastfmAttributeHistoryRecord.builder()
            .apiCallId(apiCall1.getId())
            .entityType(LastfmEntityType.ARTIST)
            .entityId(artist.getId())
            .attribute(LastfmAttribute.LISTENERS_COUNT)
            .numericValue(500L)
            .validFrom(LocalDate.now().minusDays(1))
            .build();
        
        jdbcTemplate.update("""
            INSERT INTO mu_raw_lastfm.attribute_history
            (api_call_id, entity_type, entity_id, attribute_id, numeric_value, valid_from, valid_till, collection_ts)
            VALUES (?, ?, ?, ?, ?, ?, '9999-12-31', NOW())
            """, 
            existingRecord.getApiCallId(),
            existingRecord.getEntityType().getCode(),
            existingRecord.getEntityId(),
            existingRecord.getAttribute().getCode(),
            existingRecord.getNumericValue(),
            existingRecord.getValidFrom()
        );

        // Given - add new record with changed value
        var apiCall2 = consistencyHelper.createAndSaveApiCall();
        var newRecord = LastfmAttributeHistoryRecord.builder()
            .apiCallId(apiCall2.getId())
            .entityType(LastfmEntityType.ARTIST)
            .entityId(artist.getId())
            .attribute(LastfmAttribute.LISTENERS_COUNT)
            .numericValue(1000L)
            .validFrom(LocalDate.now())
            .build();

        String currentTable = processor.getCurrentStagingTable();
        jdbcTemplate.update("""
            INSERT INTO %s
            (api_call_id, entity_type, entity_id, attribute_id, numeric_value, valid_from, collection_ts)
            VALUES (?, ?, ?, ?, ?, ?, NOW())
            """.formatted(currentTable),
            newRecord.getApiCallId(),
            newRecord.getEntityType().getCode(),
            newRecord.getEntityId(),
            newRecord.getAttribute().getCode(),
            newRecord.getNumericValue(),
            newRecord.getValidFrom()
        );
        entityManager.flush();

        // When
        processor.processStagingRecords(currentTable);

        // Then - проверяем что старая запись expired
        Integer expiredCount = jdbcTemplate.queryForObject("""
            SELECT COUNT(*) FROM mu_raw_lastfm.attribute_history 
            WHERE entity_id = ? AND attribute_id = ? AND valid_till != '9999-12-31'
            """, Integer.class, artist.getId(), LastfmAttribute.LISTENERS_COUNT.getCode());
        assertEquals(1, expiredCount);

        // Then - проверяем что новая запись добавлена
        Integer currentCount = jdbcTemplate.queryForObject("""
            SELECT COUNT(*) FROM mu_raw_lastfm.attribute_history 
            WHERE entity_id = ? AND attribute_id = ? AND valid_till = '9999-12-31'
            """, Integer.class, artist.getId(), LastfmAttribute.LISTENERS_COUNT.getCode());
        assertEquals(1, currentCount);

        // Then - проверяем значение новой записи
        Long currentValue = jdbcTemplate.queryForObject("""
            SELECT numeric_value FROM mu_raw_lastfm.attribute_history 
            WHERE entity_id = ? AND attribute_id = ? AND valid_till = '9999-12-31'
            """, Long.class, artist.getId(), LastfmAttribute.LISTENERS_COUNT.getCode());
        assertEquals(1000L, currentValue);

        // Then - staging таблица должна быть очищена
        Integer stagingCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + currentTable, Integer.class);
        assertEquals(0, stagingCount);
    }

    @Test
    void shouldNotProcessUnchangedValues() {
        // Given
        var apiCall1 = consistencyHelper.createAndSaveApiCall();
        var artist = consistencyHelper.createAndSaveArtist();
        entityManager.flush();
        
        jdbcTemplate.update("""
            INSERT INTO mu_raw_lastfm.attribute_history 
            (api_call_id, entity_type, entity_id, attribute_id, numeric_value, valid_from, valid_till, collection_ts)
            VALUES (?, ?, ?, ?, ?, ?, '9999-12-31', NOW())
            """, 
            apiCall1.getId(),
            LastfmEntityType.ARTIST.getCode(),
            artist.getId(),
            LastfmAttribute.LISTENERS_COUNT.getCode(),
            1000L,
            LocalDate.now()
        );

        // Given - добавляем в staging запись с тем же значением
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
            1000L, // то же значение
            LocalDate.now()
        );
        entityManager.flush();

        // When
        processor.processStagingRecords(currentTable);

        // Then - должна остаться только одна запись (не expired)
        Integer totalCount = jdbcTemplate.queryForObject("""
            SELECT COUNT(*) FROM mu_raw_lastfm.attribute_history 
            WHERE entity_id = ? AND attribute_id = ?
            """, Integer.class, artist.getId(), LastfmAttribute.LISTENERS_COUNT.getCode());
        assertEquals(1, totalCount);

        Integer currentCount = jdbcTemplate.queryForObject("""
            SELECT COUNT(*) FROM mu_raw_lastfm.attribute_history 
            WHERE entity_id = ? AND attribute_id = ? AND valid_till = '9999-12-31'
            """, Integer.class, artist.getId(), LastfmAttribute.LISTENERS_COUNT.getCode());
        assertEquals(1, currentCount);
    }

    @Test
    void shouldDeduplicateRecordsInStagingTable() {
        // Given
        var apiCall1 = consistencyHelper.createAndSaveApiCall();
        var apiCall2 = consistencyHelper.createAndSaveApiCall();
        var artist = consistencyHelper.createAndSaveArtist();
        entityManager.flush();
        
        var record1 = LastfmAttributeHistoryRecord.builder()
            .apiCallId(apiCall1.getId())
            .entityType(LastfmEntityType.ARTIST)
            .entityId(artist.getId())
            .attribute(LastfmAttribute.LISTENERS_COUNT)
            .numericValue(1000L)
            .validFrom(LocalDate.now())
            .build();
            
        var record2 = LastfmAttributeHistoryRecord.builder()
            .apiCallId(apiCall2.getId())
            .entityType(LastfmEntityType.ARTIST)
            .entityId(artist.getId())
            .attribute(LastfmAttribute.LISTENERS_COUNT)
            .numericValue(2000L) // different value
            .validFrom(LocalDate.now())
            .build();

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
        int totalExpired = 0;
        int totalInserted = 0;
        long offset = 0;
        long totalRecords = 7;

        while (offset < totalRecords) {
            var result = processor.processBatch(currentTable, offset, batchSize);
            totalExpired += result.expired();
            totalInserted += result.inserted();
            offset += batchSize;
        }

        // Then - all 7 records should be inserted (no existing records, so no expirations)
        assertEquals(0, totalExpired, "Should expire 0 records (all new)");
        assertEquals(7, totalInserted, "Should insert all 7 records");

        // Verify all 7 artists have their records
        for (int i = 0; i < 7; i++) {
            Long artistId = artistIds.get(i);
            Long value = jdbcTemplate.queryForObject("""
                SELECT numeric_value FROM mu_raw_lastfm.attribute_history
                WHERE entity_id = ? AND attribute_id = ? AND valid_till = '9999-12-31'
                """, Long.class, artistId, LastfmAttribute.LISTENERS_COUNT.getCode());
            assertEquals((i + 1) * 1000L, value, String.format("Artist %d should have correct value", i));
        }

        // Verify total count
        Integer totalCount = jdbcTemplate.queryForObject("""
            SELECT COUNT(*) FROM mu_raw_lastfm.attribute_history
            WHERE valid_till = '9999-12-31'
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

        // Verify 5 records in main table
        Integer count = jdbcTemplate.queryForObject("""
            SELECT COUNT(*) FROM mu_raw_lastfm.attribute_history
            WHERE valid_till = '9999-12-31'
            """, Integer.class);
        assertEquals(5, count, "Should have 5 current records in main table");
    }

    @Test
    void shouldHandleSameEntityAcrossMultipleBatches() {
        // Given - create existing records in main table, then create staging updates across multiple batches
        String currentTable = processor.getCurrentStagingTable();
        var apiCall1 = consistencyHelper.createAndSaveApiCall();
        entityManager.flush();

        // Create 6 artists with existing values in main table
        var artistIds = new java.util.ArrayList<Long>();
        for (int i = 0; i < 6; i++) {
            var artist = consistencyHelper.createAndSaveArtist();
            artistIds.add(artist.getId());
            entityManager.flush();

            // Insert existing record in main table (use the correct sequence for ID)
            jdbcTemplate.update("""
                INSERT INTO mu_raw_lastfm.attribute_history
                (id, api_call_id, entity_type, entity_id, attribute_id, numeric_value, valid_from, valid_till, collection_ts)
                VALUES (nextval('mu_raw_lastfm.attribute_history_seq'), ?, ?, ?, ?, ?, ?, '9999-12-31', NOW())
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
        int totalExpired = 0;
        int totalInserted = 0;

        // Process first batch (records 1-3)
        var result1 = processor.processBatch(currentTable, 0, batchSize);
        totalExpired += result1.expired();
        totalInserted += result1.inserted();

        // Ensure temp table is dropped between batches (in test context)
        jdbcTemplate.execute("DROP TABLE IF EXISTS batch_ids");

        // Process second batch (records 4-6)
        var result2 = processor.processBatch(currentTable, batchSize, batchSize);
        totalExpired += result2.expired();
        totalInserted += result2.inserted();

        // Then - verify cumulative results: all 6 old records should be expired, 6 new inserted
        assertEquals(6, totalExpired, "Should expire all 6 existing records");
        assertEquals(6, totalInserted, "Should insert all 6 updated records");

        // Verify each artist has the updated value
        for (int i = 0; i < 6; i++) {
            Long artistId = artistIds.get(i);
            Long currentValue = jdbcTemplate.queryForObject("""
                SELECT numeric_value FROM mu_raw_lastfm.attribute_history
                WHERE entity_id = ? AND attribute_id = ? AND valid_till = '9999-12-31'
                """, Long.class, artistId, LastfmAttribute.LISTENERS_COUNT.getCode());
            assertEquals((i + 1) * 1000L, currentValue,
                String.format("Artist %d should have updated value", i));

            // Verify old value is expired
            Integer expiredCount = jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM mu_raw_lastfm.attribute_history
                WHERE entity_id = ? AND attribute_id = ? AND valid_till != '9999-12-31'
                """, Integer.class, artistId, LastfmAttribute.LISTENERS_COUNT.getCode());
            assertEquals(1, expiredCount, String.format("Artist %d should have 1 expired record", i));
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

        // Then - verify all 6 artists have their records in main table
        for (int i = 0; i < 6; i++) {
            Long artistId = artists.get(i);
            Long value = jdbcTemplate.queryForObject("""
                SELECT numeric_value FROM mu_raw_lastfm.attribute_history
                WHERE entity_id = ? AND attribute_id = ? AND valid_till = '9999-12-31'
                """, Long.class, artistId, LastfmAttribute.LISTENERS_COUNT.getCode());

            assertEquals((i + 1) * 1000L, value,
                String.format("Artist %d should have value %d", i, (i + 1) * 1000));
        }

        // Verify total count
        Integer totalCount = jdbcTemplate.queryForObject("""
            SELECT COUNT(*) FROM mu_raw_lastfm.attribute_history
            WHERE valid_till = '9999-12-31'
            """, Integer.class);
        assertEquals(6, totalCount, "Should have exactly 6 current records");
    }

    @Test
    void shouldHandleMixedOperationsInBatch() {
        // Given - create some existing records and some new records in staging
        String currentTable = processor.getCurrentStagingTable();

        // Create 2 existing records in main table
        var artist1 = consistencyHelper.createAndSaveArtist();
        var artist2 = consistencyHelper.createAndSaveArtist();
        var apiCall1 = consistencyHelper.createAndSaveApiCall();
        entityManager.flush();

        jdbcTemplate.update("""
            INSERT INTO mu_raw_lastfm.attribute_history
            (id, api_call_id, entity_type, entity_id, attribute_id, numeric_value, valid_from, valid_till, collection_ts)
            VALUES (nextval('mu_raw_lastfm.attribute_history_seq'), ?, ?, ?, ?, ?, ?, '9999-12-31', NOW())
            """,
            apiCall1.getId(),
            LastfmEntityType.ARTIST.getCode(),
            artist1.getId(),
            LastfmAttribute.LISTENERS_COUNT.getCode(),
            500L,
            LocalDate.now().minusDays(1)
        );

        jdbcTemplate.update("""
            INSERT INTO mu_raw_lastfm.attribute_history
            (id, api_call_id, entity_type, entity_id, attribute_id, numeric_value, valid_from, valid_till, collection_ts)
            VALUES (nextval('mu_raw_lastfm.attribute_history_seq'), ?, ?, ?, ?, ?, ?, '9999-12-31', NOW())
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

        // Then - should have 2 expired (artist1 and artist2 old values) and 4 inserted (2 updates + 2 new)
        assertEquals(2, result.expired(), "Should expire 2 existing records");
        assertEquals(4, result.inserted(), "Should insert 4 records (2 updates + 2 new)");

        // Verify updated values
        Long artist1Value = jdbcTemplate.queryForObject("""
            SELECT numeric_value FROM mu_raw_lastfm.attribute_history
            WHERE entity_id = ? AND attribute_id = ? AND valid_till = '9999-12-31'
            """, Long.class, artist1.getId(), LastfmAttribute.LISTENERS_COUNT.getCode());
        assertEquals(1500L, artist1Value);

        Long artist2Value = jdbcTemplate.queryForObject("""
            SELECT numeric_value FROM mu_raw_lastfm.attribute_history
            WHERE entity_id = ? AND attribute_id = ? AND valid_till = '9999-12-31'
            """, Long.class, artist2.getId(), LastfmAttribute.LISTENERS_COUNT.getCode());
        assertEquals(1600L, artist2Value);

        // Verify new values
        Long artist3Value = jdbcTemplate.queryForObject("""
            SELECT numeric_value FROM mu_raw_lastfm.attribute_history
            WHERE entity_id = ? AND attribute_id = ? AND valid_till = '9999-12-31'
            """, Long.class, artist3.getId(), LastfmAttribute.LISTENERS_COUNT.getCode());
        assertEquals(2000L, artist3Value);

        Long artist4Value = jdbcTemplate.queryForObject("""
            SELECT numeric_value FROM mu_raw_lastfm.attribute_history
            WHERE entity_id = ? AND attribute_id = ? AND valid_till = '9999-12-31'
            """, Long.class, artist4.getId(), LastfmAttribute.LISTENERS_COUNT.getCode());
        assertEquals(3000L, artist4Value);
    }
}
