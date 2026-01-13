package yurykorzun.art.universe.common.service.lookup;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yurykorzun.art.universe.common.domain.dto.lookup.BaseBatchLookupRequestDTO;
import yurykorzun.art.universe.common.domain.dto.lookup.BatchLookupResponseDTO;
import yurykorzun.art.universe.common.domain.dto.lookup.LookupRequestDTO;
import yurykorzun.art.universe.common.domain.dto.lookup.LookupResultDTO;
import yurykorzun.art.universe.common.domain.entity.BaseEntityMetadata;
import yurykorzun.art.universe.common.domain.entity.EntityType;
import yurykorzun.art.universe.common.domain.service.lookup.BaseLookupService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BaseLookupServiceTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private Query query;

    private TestLookupService lookupService;

    @BeforeEach
    void setUp() {
        lenient().when(entityManager.createNativeQuery(anyString())).thenReturn(query);
        lenient().when(query.setParameter(anyInt(), any())).thenReturn(query);
        lookupService = new TestLookupService(entityManager, TestEntityType.TEST_ENTITY);
    }

    @Test
    void lookup_shouldBuildCorrectSqlAndParameters() {
        // Given
        String searchTerm = "test";
        int limit = 10;

        LookupRequestDTO request = LookupRequestDTO.builder()
            .search(searchTerm)
            .limit(limit)
            .build();

        List<Object[]> mockResults = new ArrayList<>();
        mockResults.add(new Object[]{1L, "Test Entity"});
        when(query.getResultList()).thenReturn(mockResults);

        // When
        List<LookupResultDTO> result = lookupService.lookup(request);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());

        // Verify SQL contains correct table name
        verify(entityManager).createNativeQuery(contains("test_entity"));

        // Verify parameters
        verify(query).setParameter(1, searchTerm);
        verify(query).setParameter(2, limit);
    }

    @Test
    void batchLookup_shouldCallLookupForEachRequest() {
        // Given
        LookupRequestDTO request1 = LookupRequestDTO.builder()
            .search("test1")
            .limit(10)
            .build();

        LookupRequestDTO request2 = LookupRequestDTO.builder()
            .search("test2")
            .limit(5)
            .build();

        BaseBatchLookupRequestDTO batchRequest = BaseBatchLookupRequestDTO.builder()
            .searchRequests(Arrays.asList(request1, request2))
            .limit(20)
            .build();

        // Mock results for each lookup call
        List<Object[]> mockResults1 = new ArrayList<>();
        mockResults1.add(new Object[]{1L, "Entity 1"});
        mockResults1.add(new Object[]{2L, "Entity 2"});
        
        List<Object[]> mockResults2 = new ArrayList<>();
        mockResults2.add(new Object[]{3L, "Entity 3"});
        
        when(query.getResultList())
            .thenReturn(mockResults1)
            .thenReturn(mockResults2);

        // When
        BatchLookupResponseDTO result = lookupService.batchLookup(batchRequest);

        // Then
        // Verify SQL was executed twice (once for each request)
        verify(entityManager, times(2)).createNativeQuery(anyString());
        
        // Verify parameters for both requests
        verify(query).setParameter(1, "test1");
        verify(query).setParameter(1, "test2");
        verify(query).setParameter(2, 10);
        verify(query).setParameter(2, 5);
        
        // Verify getResultList was called twice
        verify(query, times(2)).getResultList();

        // Verify results are correctly mapped
        Map<String, List<LookupResultDTO>> resultMap = result.getResults();
        assertEquals(2, resultMap.size());
        assertEquals(2, resultMap.get("test1").size());
        assertEquals(1, resultMap.get("test2").size());
        assertEquals("Entity 1", resultMap.get("test1").get(0).getName());
        assertEquals("Entity 2", resultMap.get("test1").get(1).getName());
        assertEquals("Entity 3", resultMap.get("test2").get(0).getName());
    }

    @Test
    void lookup_withTrimmedSearchTerm_shouldTrimBeforeQuery() {
        // Given
        String searchTerm = "  test  ";
        String trimmedSearchTerm = "test";

        LookupRequestDTO request = LookupRequestDTO.builder()
            .search(searchTerm)
            .limit(10)
            .build();

        when(query.getResultList()).thenReturn(new ArrayList<>());

        // When
        lookupService.lookup(request);

        // Then
        verify(query).setParameter(1, trimmedSearchTerm);
    }

    @Test
    void batchLookup_withMixedLimits_shouldUseCorrectLimits() {
        // Given
        LookupRequestDTO request1 = LookupRequestDTO.builder()
            .search("test1")
            .limit(5) // Specific limit
            .build();

        LookupRequestDTO request2 = LookupRequestDTO.builder()
            .search("test2")
            .limit(null) // No limit, should use batch limit
            .build();

        BaseBatchLookupRequestDTO batchRequest = BaseBatchLookupRequestDTO.builder()
            .searchRequests(Arrays.asList(request1, request2))
            .limit(10) // Batch limit
            .build();

        when(query.getResultList()).thenReturn(new ArrayList<>());

        // When
        lookupService.batchLookup(batchRequest);

        // Then
        // First request should use its own limit
        verify(query).setParameter(2, 5);
        
        // Second request should use batch limit
        verify(query).setParameter(2, 10);
    }

    @Test
    void batchLookup_withNullBatchLimit_shouldUseDefaultLimit() {
        // Given
        LookupRequestDTO request = LookupRequestDTO.builder()
            .search("test")
            .limit(null)
            .build();

        BaseBatchLookupRequestDTO batchRequest = BaseBatchLookupRequestDTO.builder()
            .searchRequests(List.of(request))
            .limit(null) // No batch limit
            .build();

        when(query.getResultList()).thenReturn(new ArrayList<>());

        // When
        lookupService.batchLookup(batchRequest);

        // Then
        verify(query).setParameter(2, 20); // Should use default limit of 20
    }

    @Test
    void lookup_shouldUsePriorityOrderingInSql() {
        // Given
        LookupRequestDTO request = LookupRequestDTO.builder()
            .search("test")
            .limit(10)
            .build();

        when(query.getResultList()).thenReturn(new ArrayList<>());

        // When
        lookupService.lookup(request);

        // Then
        // Verify SQL contains priority ordering logic
        verify(entityManager).createNativeQuery(argThat(sql -> 
            sql.contains("CASE") && 
            sql.contains("WHEN LOWER(e.name) = LOWER(?1) THEN 0") &&
            sql.contains("WHEN LOWER(e.name) LIKE LOWER(CONCAT(?1, '%')) THEN 1") &&
            sql.contains("ELSE 2")
        ));
    }

    // Test implementations
    private enum TestEntityType implements EntityType {
        TEST_ENTITY(1, "test_entity");

        private final int code;
        private final String name;

        TestEntityType(int code, String name) {
            this.code = code;
            this.name = name;
        }

        @Override
        public Integer getCode() {
            return code;
        }

        @Override
        public String getName() {
            return name;
        }
    }

    // Test implementation of BaseLookupService
    private static class TestLookupService extends BaseLookupService<TestEntityType, BaseEntityMetadata<TestEntityType>> {

        public TestLookupService(EntityManager entityManager, TestEntityType entityType) {
            super(entityManager, entityType);
        }

        @Override
        protected BaseEntityMetadata<TestEntityType> createEntityMetadata() {
            return new BaseEntityMetadata<>(getEntityType());
        }
    }
}
