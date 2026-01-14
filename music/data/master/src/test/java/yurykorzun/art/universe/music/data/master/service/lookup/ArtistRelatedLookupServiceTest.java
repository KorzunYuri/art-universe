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
import yurykorzun.art.universe.music.data.master.dto.lookup.ArtistRelatedBatchLookupRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.lookup.ArtistRelatedLookupRequestDTO;
import yurykorzun.art.universe.common.domain.dto.lookup.BatchLookupResponseDTO;
import yurykorzun.art.universe.common.domain.dto.lookup.LookupResultDTO;
import yurykorzun.art.universe.music.data.master.entity.DataSource;
import yurykorzun.art.universe.music.data.master.entity.MasterEntityType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ArtistRelatedLookupServiceTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private Query query;

    private ArtistRelatedLookupService lookupService;

    @BeforeEach
    void setUp() {
        lenient().when(entityManager.createNativeQuery(anyString())).thenReturn(query);
        lenient().when(query.setParameter(anyInt(), any())).thenReturn(query);
    }

    @ParameterizedTest
    @EnumSource(value = MasterEntityType.class, names = {"TRACK", "ALBUM"})
    void lookup_shouldBuildCorrectSqlWithArtistJoin(MasterEntityType entityType) {
        // Given
        lookupService = new ArtistRelatedLookupService(entityManager, entityType);
        String searchTerm = "paranoid";
        Long artistId = 123L;
        
        ArtistRelatedLookupRequestDTO request = ArtistRelatedLookupRequestDTO.builder()
                .search(searchTerm)
                .masterArtistId(artistId)
                .dataSource(DataSource.LASTFM)
                .limit(10)
                .build();

        // Mock query results with artist data
        List<Object[]> results = new ArrayList<>();
        results.add(new Object[]{1L, "Paranoid Android", 100L, "Radiohead"});
        when(query.getResultList()).thenReturn(results);

        // When
        List<LookupResultDTO> result = lookupService.lookup(request);

        // Then
        assertEquals(1, result.size());
        assertEquals("Radiohead - Paranoid Android", result.get(0).getName());
        
        // Verify SQL contains JOIN with artist
        verify(entityManager).createNativeQuery(matches("JOIN[ \\n]+artist"));
    }

    @Test
    void lookup_withExternalArtistId_shouldSetCorrectParameters() {
        // Given
        lookupService = new ArtistRelatedLookupService(entityManager, MasterEntityType.TRACK);
        String searchTerm = "paranoid";
        Long externalArtistId = 456L;
        
        ArtistRelatedLookupRequestDTO request = ArtistRelatedLookupRequestDTO.builder()
                .search(searchTerm)
                .externalArtistId(externalArtistId)
                .dataSource(DataSource.LASTFM)
                .limit(10)
                .build();

        // Mock query results
        List<Object[]> results = new ArrayList<>();
        results.add(new Object[]{1L, "Paranoid Android", 100L, "Radiohead"});
        when(query.getResultList()).thenReturn(results);

        // When
        List<LookupResultDTO> result = lookupService.lookup(request);

        // Then
        assertEquals(1, result.size());
        
        // Verify SQL contains the artist_binding table for external artist ID lookup
        verify(entityManager).createNativeQuery(contains("artist_binding ab"));
    }

    @ParameterizedTest
    @EnumSource(value = MasterEntityType.class, names = {"TRACK", "ALBUM"})
    void batchLookup_shouldCallLookupForEachRequest(MasterEntityType entityType) {
        // Given
        lookupService = new ArtistRelatedLookupService(entityManager, entityType);
        
        ArtistRelatedLookupRequestDTO request1 = ArtistRelatedLookupRequestDTO.builder()
                .search("paranoid")
                .masterArtistId(123L)
                .dataSource(DataSource.LASTFM)
                .limit(10)
                .build();
        
        ArtistRelatedLookupRequestDTO request2 = ArtistRelatedLookupRequestDTO.builder()
                .search("karma")
                .masterArtistId(123L)
                .dataSource(DataSource.LASTFM)
                .limit(5)
                .build();
        
        ArtistRelatedBatchLookupRequestDTO batchRequest = ArtistRelatedBatchLookupRequestDTO.builder()
                .searchRequests(Arrays.asList(request1, request2))
                .limit(20)
                .build();

        // Mock query results for each request
        List<Object[]> results1 = new ArrayList<>();
        results1.add(new Object[]{1L, "Paranoid Android", 123L, "Radiohead"});
        
        List<Object[]> results2 = new ArrayList<>();
        results2.add(new Object[]{2L, "Karma Police", 123L, "Radiohead"});
        
        when(query.getResultList())
            .thenReturn(results1)
            .thenReturn(results2);

        // When
        BatchLookupResponseDTO result = lookupService.batchLookup(batchRequest);

        // Then
        // Verify SQL contains JOIN with artist for both queries
        verify(entityManager, times(2)).createNativeQuery(matches("JOIN[ \\n]+artist"));
        
        // Verify results are correctly mapped with artist names
        Map<String, List<LookupResultDTO>> resultMap = result.getResults();
        assertEquals(2, resultMap.size());
        assertEquals(1, resultMap.get("paranoid").size());
        assertEquals(1, resultMap.get("karma").size());
        assertEquals("Radiohead - Paranoid Android", resultMap.get("paranoid").get(0).getName());
        assertEquals("Radiohead - Karma Police", resultMap.get("karma").get(0).getName());
    }
    
    @Test
    void batchLookup_withMixedArtistIdTypes_shouldCreateCorrectQueries() {
        // Given
        lookupService = new ArtistRelatedLookupService(entityManager, MasterEntityType.TRACK);
        
        ArtistRelatedLookupRequestDTO request1 = ArtistRelatedLookupRequestDTO.builder()
                .search("paranoid")
                .masterArtistId(123L)
                .dataSource(DataSource.LASTFM)
                .limit(10)
                .build();
        
        ArtistRelatedLookupRequestDTO request2 = ArtistRelatedLookupRequestDTO.builder()
                .search("karma")
                .externalArtistId(456L)
                .dataSource(DataSource.LASTFM)
                .limit(5)
                .build();
        
        ArtistRelatedBatchLookupRequestDTO batchRequest = ArtistRelatedBatchLookupRequestDTO.builder()
                .searchRequests(Arrays.asList(request1, request2))
                .limit(20)
                .build();

        when(query.getResultList()).thenReturn(new ArrayList<>());

        // When
        lookupService.batchLookup(batchRequest);

        // Then
        // Verify SQL was created twice
        verify(entityManager, times(2)).createNativeQuery(anyString());
    }
    
    @Test
    void lookup_withEmptySearchTerm_withoutArtistId_shouldReturnEmptyList() {
        // Given
        lookupService = new ArtistRelatedLookupService(entityManager, MasterEntityType.TRACK);
        
        ArtistRelatedLookupRequestDTO request = ArtistRelatedLookupRequestDTO.builder()
                .search("")
                .dataSource(DataSource.LASTFM)
                .limit(10)
                .build();
        
        // When
        List<LookupResultDTO> result = lookupService.lookup(request);
        
        // Then
        assertTrue(result.isEmpty());
        verifyNoInteractions(query);
    }
    
    @Test
    void lookup_withNullSearchTerm_withoutArtistId_shouldReturnEmptyList() {
        // Given
        lookupService = new ArtistRelatedLookupService(entityManager, MasterEntityType.TRACK);
        
        ArtistRelatedLookupRequestDTO request = ArtistRelatedLookupRequestDTO.builder()
                .search(null)
                .dataSource(DataSource.LASTFM)
                .limit(10)
                .build();
        
        // When
        List<LookupResultDTO> result = lookupService.lookup(request);
        
        // Then
        assertTrue(result.isEmpty());
        verifyNoInteractions(query);
    }
    
    @Test
    void lookup_withEmptySearchTerm_withMasterArtistId_shouldReturnResults() {
        // Given
        lookupService = new ArtistRelatedLookupService(entityManager, MasterEntityType.TRACK);
        
        ArtistRelatedLookupRequestDTO request = ArtistRelatedLookupRequestDTO.builder()
                .search("")
                .masterArtistId(123L)
                .dataSource(DataSource.LASTFM)
                .limit(10)
                .build();
        
        // Mock query results
        List<Object[]> results = new ArrayList<>();
        results.add(new Object[]{1L, "Paranoid Android", 123L, "Radiohead"});
        when(query.getResultList()).thenReturn(results);
        
        // When
        List<LookupResultDTO> result = lookupService.lookup(request);
        
        // Then
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        verify(entityManager).createNativeQuery(anyString());
    }
    
    @Test
    void lookup_withEmptySearchTerm_withExternalArtistId_shouldReturnResults() {
        // Given
        lookupService = new ArtistRelatedLookupService(entityManager, MasterEntityType.TRACK);
        
        ArtistRelatedLookupRequestDTO request = ArtistRelatedLookupRequestDTO.builder()
                .search("")
                .externalArtistId(456L)
                .dataSource(DataSource.LASTFM)
                .limit(10)
                .build();
        
        // Mock query results
        List<Object[]> results = new ArrayList<>();
        results.add(new Object[]{1L, "Paranoid Android", 123L, "Radiohead"});
        when(query.getResultList()).thenReturn(results);
        
        // When
        List<LookupResultDTO> result = lookupService.lookup(request);
        
        // Then
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        verify(entityManager).createNativeQuery(anyString());
    }
    
    @Test
    void lookup_withNoSearchTermAndNoArtistId_shouldReturnEmptyList() {
        // Given
        lookupService = new ArtistRelatedLookupService(entityManager, MasterEntityType.TRACK);
        
        ArtistRelatedLookupRequestDTO request = ArtistRelatedLookupRequestDTO.builder()
                .search(null)
                .dataSource(DataSource.LASTFM)
                .limit(10)
                .build();
        
        // When
        List<LookupResultDTO> result = lookupService.lookup(request);
        
        // Then
        assertTrue(result.isEmpty());
        verifyNoInteractions(query);
    }
    
    @Test
    void batchLookup_withDefaultLimit_shouldUseDefaultLimit() {
        // Given
        lookupService = new ArtistRelatedLookupService(entityManager, MasterEntityType.TRACK);
        
        ArtistRelatedLookupRequestDTO request = ArtistRelatedLookupRequestDTO.builder()
                .search("test")
                .masterArtistId(123L)
                .dataSource(DataSource.LASTFM)
                .limit(null) // No individual limit
                .build();
        
        ArtistRelatedBatchLookupRequestDTO batchRequest = ArtistRelatedBatchLookupRequestDTO.builder()
                .searchRequests(List.of(request))
                .limit(null) // No batch limit
                .build();

        when(query.getResultList()).thenReturn(new ArrayList<>());

        // When
        lookupService.batchLookup(batchRequest);

        // Then
        // Verify SQL contains the default limit
        verify(entityManager).createNativeQuery(contains("LIMIT ?"));
    }
    
    @Test
    void lookup_withExternalArtistIdButNullDataSource_shouldThrowException() {
        // Given
        lookupService = new ArtistRelatedLookupService(entityManager, MasterEntityType.TRACK);
        
        ArtistRelatedLookupRequestDTO request = ArtistRelatedLookupRequestDTO.builder()
                .search("test")
                .externalArtistId(123L)
                .dataSource(null)
                .limit(10)
                .build();
        
        // When/Then
        NullPointerException exception = assertThrows(NullPointerException.class, 
            () -> lookupService.lookup(request));
    }
    
    @ParameterizedTest
    @ValueSource(ints = {0, -1, Integer.MIN_VALUE})
    void lookup_withInvalidLimit_shouldThrowException(int invalidLimit) {
        // Given
        lookupService = new ArtistRelatedLookupService(entityManager, MasterEntityType.TRACK);
        
        ArtistRelatedLookupRequestDTO request = ArtistRelatedLookupRequestDTO.builder()
                .search("test")
                .masterArtistId(123L)
                .dataSource(DataSource.LASTFM)
                .limit(invalidLimit)
                .build();
        
        // When/Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> lookupService.lookup(request));
        assertEquals("Limit must be greater than zero", exception.getMessage());
    }
}
