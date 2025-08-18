package yurykorzun.art.universe.music.data.master.service.lookup;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yurykorzun.art.universe.common.dto.lookup.BaseBatchLookupRequestDTO;
import yurykorzun.art.universe.common.dto.lookup.BatchLookupResponseDTO;
import yurykorzun.art.universe.common.dto.lookup.LookupRequestDTO;
import yurykorzun.art.universe.common.dto.lookup.LookupResultDTO;
import yurykorzun.art.universe.music.data.master.entity.MasterEntityType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.AdditionalMatchers.not;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DimensionLookupServiceTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private Query query;

    private DimensionLookupService lookupService;

    @BeforeEach
    void setUp() {
        lookupService = new DimensionLookupService(entityManager);
        lenient().when(entityManager.createNativeQuery(anyString())).thenReturn(query);
        lenient().when(query.setParameter(anyInt(), any())).thenReturn(query);
    }

    @Test
    void lookup_withNullSearch_shouldReturnAllDimensions() {
        // Given
        LookupRequestDTO request = LookupRequestDTO.builder()
                .search(null)
                .limit(10)
                .build();

        List<Object[]> mockResults = new ArrayList<>();
        mockResults.add(new Object[]{1L, "Genre"});
        mockResults.add(new Object[]{2L, "Mood"});
        when(query.getResultList()).thenReturn(mockResults);

        // When
        List<LookupResultDTO> result = lookupService.lookup(request);

        // Then
        assertEquals(2, result.size());
        
        // Verify SQL doesn't contain WHERE clause
        verify(entityManager).createNativeQuery(not(contains("WHERE")));
        
        // Verify only limit parameter is set
        verify(query).setParameter(1, 10);
    }

    @Test
    void lookup_withEmptySearch_shouldReturnAllDimensions() {
        // Given
        LookupRequestDTO request = LookupRequestDTO.builder()
                .search("")
                .limit(10)
                .build();

        List<Object[]> mockResults = new ArrayList<>();
        mockResults.add(new Object[]{1L, "Genre"});
        mockResults.add(new Object[]{2L, "Mood"});
        when(query.getResultList()).thenReturn(mockResults);

        // When
        List<LookupResultDTO> result = lookupService.lookup(request);

        // Then
        assertEquals(2, result.size());
        
        // Verify SQL doesn't contain WHERE clause
        verify(entityManager).createNativeQuery(not(contains("WHERE")));
        
        // Verify only limit parameter is set
        verify(query).setParameter(1, 10);
    }

    @Test
    void lookup_withValidSearch_shouldFilterResults() {
        // Given
        String searchTerm = "genre";
        LookupRequestDTO request = LookupRequestDTO.builder()
                .search(searchTerm)
                .limit(10)
                .build();

        List<Object[]> mockResults = new ArrayList<>();
        mockResults.add(new Object[]{1L, "Genre"});
        when(query.getResultList()).thenReturn(mockResults);

        // When
        List<LookupResultDTO> result = lookupService.lookup(request);

        // Then
        assertEquals(1, result.size());
        assertEquals("Genre", result.get(0).getName());
        
        // Verify SQL contains WHERE clause
        verify(entityManager).createNativeQuery(contains("WHERE"));
        
        // Verify search and limit parameters are set
        verify(query).setParameter(1, searchTerm);
        verify(query).setParameter(2, 10);
    }

    @Test
    void batchLookup_withMixedSearchTerms_shouldHandleBothEmptyAndNonEmpty() {
        // Given
        LookupRequestDTO request1 = LookupRequestDTO.builder()
                .search("genre")
                .limit(5)
                .build();
        
        LookupRequestDTO request2 = LookupRequestDTO.builder()
                .search("")
                .limit(10)
                .build();
        
        BaseBatchLookupRequestDTO batchRequest = BaseBatchLookupRequestDTO.builder()
                .searchRequests(Arrays.asList(request1, request2))
                .limit(20)
                .build();

        // Mock results for each lookup call
        List<Object[]> mockResults1 = new ArrayList<>();
        mockResults1.add(new Object[]{1L, "Genre"});
        
        List<Object[]> mockResults2 = new ArrayList<>();
        mockResults2.add(new Object[]{2L, "Mood"});
        
        when(query.getResultList())
            .thenReturn(mockResults1)
            .thenReturn(mockResults2);

        // When
        BatchLookupResponseDTO result = lookupService.batchLookup(batchRequest);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getResults().size());
        assertTrue(result.getResults().containsKey("genre"));
        assertTrue(result.getResults().containsKey(""));
        assertEquals(1, result.getResults().get("genre").size());
        assertEquals(1, result.getResults().get("").size());
        
        // Verify SQL was executed twice (once for each request)
        verify(entityManager, times(2)).createNativeQuery(anyString());
        
        // Verify SQL for empty search doesn't contain WHERE
        verify(entityManager).createNativeQuery(not(contains("WHERE")));
        
        // Verify SQL for non-empty search contains WHERE
        verify(entityManager).createNativeQuery(contains("WHERE"));
    }
    
    @Test
    void batchLookup_withNullSearchRequest_shouldUseCorrectSql() {
        // Given
        LookupRequestDTO request = LookupRequestDTO.builder()
                .search(null)
                .limit(10)
                .build();
        
        BaseBatchLookupRequestDTO batchRequest = BaseBatchLookupRequestDTO.builder()
                .searchRequests(List.of(request))
                .limit(20)
                .build();

        when(query.getResultList()).thenReturn(new ArrayList<>());

        // When
        lookupService.batchLookup(batchRequest);

        // Then
        // Verify SQL doesn't contain WHERE clause for null search
        verify(entityManager).createNativeQuery(not(contains("WHERE")));
        
        // Verify only limit parameter is set
        verify(query).setParameter(1, 10);  // Limit
    }
}
