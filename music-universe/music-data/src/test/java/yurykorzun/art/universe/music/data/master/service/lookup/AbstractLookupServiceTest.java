package yurykorzun.art.universe.music.data.master.service.lookup;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yurykorzun.art.universe.music.data.master.dto.lookup.BaseBatchLookupRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.lookup.BatchLookupResponseDTO;
import yurykorzun.art.universe.music.data.master.dto.lookup.LookupRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.lookup.LookupResultDTO;
import yurykorzun.art.universe.music.data.master.entity.EntityMetadata;
import yurykorzun.art.universe.music.data.master.entity.EntityType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AbstractLookupServiceTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private Query query;

    private TestLookupService lookupService;

    @BeforeEach
    void setUp() {
        lenient().when(entityManager.createNativeQuery(anyString())).thenReturn(query);
        lenient().when(query.setParameter(anyInt(), any())).thenReturn(query);
    }

    @ParameterizedTest
    @EnumSource(EntityType.class)
    void lookup_withValidRequest_shouldReturnResults(EntityType entityType) {
        // Given
        lookupService = new TestLookupService(entityManager, entityType);
        LookupRequestDTO request = LookupRequestDTO.builder()
                .search("test")
                .limit(10)
                .build();

        List<Object[]> mockResults = new ArrayList<>();
        mockResults.add(new Object[]{1L, "Test Entity"});
        when(query.getResultList()).thenReturn(mockResults);

        // When
        List<LookupResultDTO> result = lookupService.lookup(request);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals("Test Entity", result.get(0).getName());

        verify(entityManager).createNativeQuery(anyString());
        verify(query).setParameter(1, "test");
        verify(query).setParameter(2, 10);
        verify(query).getResultList();
    }

    @ParameterizedTest
    @EnumSource(EntityType.class)
    void lookup_withNullSearch_shouldReturnEmptyList(EntityType entityType) {
        // Given
        lookupService = new TestLookupService(entityManager, entityType);
        LookupRequestDTO request = LookupRequestDTO.builder()
                .search(null)
                .limit(10)
                .build();

        // When
        List<LookupResultDTO> result = lookupService.lookup(request);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verifyNoInteractions(query);
    }

    @ParameterizedTest
    @EnumSource(EntityType.class)
    void lookup_withEmptySearch_shouldReturnEmptyList(EntityType entityType) {
        // Given
        lookupService = new TestLookupService(entityManager, entityType);
        LookupRequestDTO request = LookupRequestDTO.builder()
                .search("")
                .limit(10)
                .build();

        // When
        List<LookupResultDTO> result = lookupService.lookup(request);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verifyNoInteractions(query);
    }

    @ParameterizedTest
    @EnumSource(EntityType.class)
    void lookup_withDefaultLimit_shouldUse20(EntityType entityType) {
        // Given
        lookupService = new TestLookupService(entityManager, entityType);
        LookupRequestDTO request = LookupRequestDTO.builder()
                .search("test")
                .limit(null)
                .build();

        when(query.getResultList()).thenReturn(new ArrayList<>());

        // When
        lookupService.lookup(request);

        // Then
        verify(query).setParameter(2, 20); // Default limit
    }

    @ParameterizedTest
    @EnumSource(EntityType.class)
    void batchLookup_withValidRequests_shouldReturnBatchResults(EntityType entityType) {
        // Given
        lookupService = new TestLookupService(entityManager, entityType);
        
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

        // Mock lookup results for each request
        List<Object[]> mockResults1 = new ArrayList<>();
        mockResults1.add(new Object[]{1L, "Entity 1"});
        mockResults1.add(new Object[]{2L, "Entity 2"});
        
        List<Object[]> mockResults2 = new ArrayList<>();
        mockResults2.add(new Object[]{3L, "Entity 3"});
        
        // Setup mock to return different results for each query
        when(query.getResultList())
            .thenReturn(mockResults1)
            .thenReturn(mockResults2);

        // When
        BatchLookupResponseDTO result = lookupService.batchLookup(batchRequest);

        // Then
        assertNotNull(result);
        assertNotNull(result.getResults());
        assertEquals(2, result.getResults().size());
        assertTrue(result.getResults().containsKey("test1"));
        assertTrue(result.getResults().containsKey("test2"));
        assertEquals(2, result.getResults().get("test1").size());
        assertEquals(1, result.getResults().get("test2").size());

        // Verify that createNativeQuery was called twice (once for each lookup)
        verify(entityManager, times(2)).createNativeQuery(anyString());
        // Verify that getResultList was called twice
        verify(query, times(2)).getResultList();
    }

    @ParameterizedTest
    @EnumSource(EntityType.class)
    void batchLookup_withNullRequests_shouldReturnEmptyResults(EntityType entityType) {
        // Given
        lookupService = new TestLookupService(entityManager, entityType);
        BaseBatchLookupRequestDTO batchRequest = BaseBatchLookupRequestDTO.builder()
                .searchRequests(null)
                .limit(10)
                .build();

        // When
        BatchLookupResponseDTO result = lookupService.batchLookup(batchRequest);

        // Then
        assertNotNull(result);
        assertNotNull(result.getResults());
        assertTrue(result.getResults().isEmpty());
        verifyNoInteractions(query);
    }

    @ParameterizedTest
    @EnumSource(EntityType.class)
    void batchLookup_withEmptyRequests_shouldReturnEmptyResults(EntityType entityType) {
        // Given
        lookupService = new TestLookupService(entityManager, entityType);
        BaseBatchLookupRequestDTO batchRequest = BaseBatchLookupRequestDTO.builder()
                .searchRequests(List.of())
                .limit(10)
                .build();

        // When
        BatchLookupResponseDTO result = lookupService.batchLookup(batchRequest);

        // Then
        assertNotNull(result);
        assertNotNull(result.getResults());
        assertTrue(result.getResults().isEmpty());
        verifyNoInteractions(query);
    }

    @ParameterizedTest
    @EnumSource(EntityType.class)
    void batchLookup_withInvalidRequests_shouldFilterThem(EntityType entityType) {
        // Given
        lookupService = new TestLookupService(entityManager, entityType);
        
        LookupRequestDTO validRequest = LookupRequestDTO.builder()
                .search("valid")
                .build();
        
        LookupRequestDTO emptyRequest = LookupRequestDTO.builder()
                .search("")
                .build();
        
        LookupRequestDTO nullRequest = LookupRequestDTO.builder()
                .search(null)
                .build();
        
        BaseBatchLookupRequestDTO batchRequest = BaseBatchLookupRequestDTO.builder()
                .searchRequests(Arrays.asList(validRequest, emptyRequest, nullRequest))
                .limit(10)
                .build();

        List<Object[]> mockResults = new ArrayList<>();
        mockResults.add(new Object[]{1L, "Valid Entity"});
        when(query.getResultList()).thenReturn(mockResults);

        // When
        BatchLookupResponseDTO result = lookupService.batchLookup(batchRequest);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getResults().size());
        assertTrue(result.getResults().containsKey("valid"));
        
        verify(entityManager).createNativeQuery(anyString());
        verify(query, times(2)).setParameter(anyInt(), any()); // Only for valid request
        verify(query).getResultList();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, Integer.MIN_VALUE})
    void lookup_withInvalidLimit_shouldThrowException(int invalidLimit) {
        // Given
        lookupService = new TestLookupService(entityManager, EntityType.ARTIST);
        LookupRequestDTO request = LookupRequestDTO.builder()
                .search("test")
                .limit(invalidLimit)
                .build();

        // When/Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> lookupService.lookup(request));
        assertEquals("Limit must be greater than zero", exception.getMessage());
    }

    @Test
    void lookup_withZeroLimit_shouldThrowException() {
        // Given
        lookupService = new TestLookupService(entityManager, EntityType.ARTIST);
        LookupRequestDTO request = LookupRequestDTO.builder()
                .search("test")
                .limit(0)
                .build();

        // When/Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> lookupService.lookup(request));
        assertEquals("Limit must be greater than zero", exception.getMessage());
    }

    @Test
    void lookup_withNegativeLimit_shouldThrowException() {
        // Given
        lookupService = new TestLookupService(entityManager, EntityType.ARTIST);
        LookupRequestDTO request = LookupRequestDTO.builder()
                .search("test")
                .limit(-1)
                .build();

        // When/Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> lookupService.lookup(request));
        assertEquals("Limit must be greater than zero", exception.getMessage());
    }

    // Simple test implementation of AbstractLookupService
    private static class TestLookupService extends AbstractLookupService<LookupRequestDTO> {
        
        public TestLookupService(EntityManager entityManager, EntityType entityType) {
            super(entityManager, entityType);
        }

        @Override
        protected SqlQueryBuilder.QueryData buildQuery(EntityMetadata metadata, LookupRequestDTO request, int limit) {
            return new SqlQueryBuilder()
                .append(String.format(
                    "SELECT e.id, e.name FROM %s e " +
                        "WHERE LOWER(e.name) LIKE LOWER(CONCAT('%%', ?1, '%%')) " +
                        "ORDER BY e.name ASC " +
                        "LIMIT ?2",
                    metadata.getTableName()
                ))
                .param(1, request.getSearch())
                .param(2, request.getLimit() != null ? request.getLimit() : limit)
                .build();
        }

        @Override
        protected List<LookupResultDTO> mapResultsToDto(List<Object[]> results) {
            return results.stream()
                .map(row -> LookupResultDTO.builder()
                    .id(((Number) row[0]).longValue())
                    .name((String) row[1])
                    .build())
                .toList();
        }

        @Override
        protected LookupRequestDTO prepareRequest(LookupRequestDTO request, int defaultLimit) {
            return LookupRequestDTO.builder()
                .search(request.getSearch())
                .limit(request.getLimit() != null ? request.getLimit() : defaultLimit)
                .build();
        }
    }
}
