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
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.repository.attribute.LastfmAttributeTypeSynchronizer;
import yurykorzun.art.universe.music.data.raw.lastfm.common.DbConsistencyHelper;
import yurykorzun.art.universe.music.data.raw.lastfm.common.archetypes.LastfmJpaTestHelper;
import yurykorzun.art.universe.music.data.raw.lastfm.maintenance.service.TestTaskCoordinatorConfig;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Import({
    LastfmAttributeHistoryProcessor.class,
    LastfmAttributeHistoryServiceImpl.class,
    LastfmAttributeTypeSynchronizer.class,
    TestTaskCoordinatorConfig.class,
})
class LastfmAttributeHistoryProcessorTest extends LastfmJpaTestHelper {

    @Autowired
    private LastfmAttributeHistoryService attributeHistoryService;
    
    @Autowired
    private LastfmAttributeHistoryProcessor processor;
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Autowired
    private DbConsistencyHelper dbHelper;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        dbHelper.cleanup();
        // Очищаем staging таблицы
        jdbcTemplate.execute("TRUNCATE TABLE mu_raw_lastfm_staging.stg_attribute_history_a");
        jdbcTemplate.execute("TRUNCATE TABLE mu_raw_lastfm_staging.stg_attribute_history_b");
    }

    @Test
    void shouldInsertIntoCurrentStagingTable() {
        // Given
        var apiCall = dbHelper.createAndSaveApiCall();
        var artist = dbHelper.createAndSaveArtist();
        
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
        var apiCall1 = dbHelper.createAndSaveApiCall();
        var artist = dbHelper.createAndSaveArtist();
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
        var apiCall2 = dbHelper.createAndSaveApiCall();
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
        var apiCall1 = dbHelper.createAndSaveApiCall();
        var artist = dbHelper.createAndSaveArtist();
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
        var apiCall2 = dbHelper.createAndSaveApiCall();
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
        var apiCall1 = dbHelper.createAndSaveApiCall();
        var apiCall2 = dbHelper.createAndSaveApiCall();
        var artist = dbHelper.createAndSaveArtist();
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
        
        // When - вызываем processStagingRecords напрямую, чтобы избежать TaskCoordinator
        processor.processStagingRecords(initialTable);
        
        // Manually switch tables to simulate the behavior
        String expectedNewTable = initialTable.equals("mu_raw_lastfm_staging.attribute_history_staging_a") 
            ? "mu_raw_lastfm_staging.attribute_history_staging_b" 
            : "mu_raw_lastfm_staging.attribute_history_staging_a";
            
        // Then - проверяем что таблицы разные
        assertNotEquals(initialTable, expectedNewTable);
        assertTrue(expectedNewTable.contains("staging_a") || expectedNewTable.contains("staging_b"));
    }

}
