package yurykorzun.art.universe.music.data.master.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yurykorzun.art.universe.music.data.master.dto.lookup.BaseBatchLookupRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.lookup.BatchLookupResponseDTO;
import yurykorzun.art.universe.music.data.master.dto.binding.EntityBindToExistingRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.binding.EntityCreateAndBindRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.lookup.LookupRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.lookup.LookupResultDTO;
import yurykorzun.art.universe.music.data.master.dto.binding.BoundEntityProjection;
import yurykorzun.art.universe.music.data.master.dto.binding.TestBoundEntityProjectionImpl;
import yurykorzun.art.universe.music.data.master.entity.DataSource;
import yurykorzun.art.universe.music.data.master.exception.DataAccessException;
import yurykorzun.art.universe.music.data.master.exception.EntityBindingException;
import yurykorzun.art.universe.music.data.master.service.ArtistService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ArtistControllerTest {

    @Mock
    private ArtistService artistService;

    @InjectMocks
    private ArtistController artistController;

    @Test
    void findBoundArtists_shouldReturnListOfBoundEntityProjections() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        List<Long> externalIds = List.of(1L, 2L);
        
        TestBoundEntityProjectionImpl projection = new TestBoundEntityProjectionImpl(
            1L, dataSource, 101L, "Test Artist"
        );

        List<BoundEntityProjection> expectedBindings = List.of(projection);
        when(artistService.findBoundArtists(dataSource, externalIds))
            .thenReturn(expectedBindings);

        // When
        List<BoundEntityProjection> result = artistController.findBoundArtists(dataSource, externalIds);

        // Then
        assertEquals(expectedBindings, result);
        verify(artistService).findBoundArtists(dataSource, externalIds);
    }
    
    @Test
    void findBoundArtists_whenExceptionThrown_shouldThrowDataAccessException() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        List<Long> externalIds = List.of(1L, 2L);
        String errorMessage = "Test error";
        
        when(artistService.findBoundArtists(dataSource, externalIds))
            .thenThrow(new RuntimeException(errorMessage));

        // When & Then
        DataAccessException exception = assertThrows(DataAccessException.class, () -> 
            artistController.findBoundArtists(dataSource, externalIds)
        );
        
        assertEquals("Failed to get bound artists: " + errorMessage, exception.getMessage());
        verify(artistService).findBoundArtists(dataSource, externalIds);
    }
    
    @Test
    void lookupArtists_shouldReturnListOfLookupResults() {
        // Given
        String search = "radio";
        LookupResultDTO artist1 = new LookupResultDTO(1L, "Radiohead");
        LookupResultDTO artist2 = new LookupResultDTO(2L, "Radio Moscow");
        List<LookupResultDTO> expectedArtists = List.of(artist1, artist2);
        
        when(artistService.lookupArtists(any(LookupRequestDTO.class))).thenReturn(expectedArtists);
            
        // When
        List<LookupResultDTO> result = artistController.lookupArtists(search, null);
            
        // Then
        assertEquals(expectedArtists, result);
        verify(artistService).lookupArtists(any(LookupRequestDTO.class));
    }
    
    @Test
    void lookupArtists_withLimit_shouldReturnListOfLookupResults() {
        // Given
        String search = "radio";
        Integer limit = 5;
        LookupResultDTO artist1 = new LookupResultDTO(1L, "Radiohead");
        LookupResultDTO artist2 = new LookupResultDTO(2L, "Radio Moscow");
        List<LookupResultDTO> expectedArtists = List.of(artist1, artist2);
        
        when(artistService.lookupArtists(any(LookupRequestDTO.class))).thenReturn(expectedArtists);
            
        // When
        List<LookupResultDTO> result = artistController.lookupArtists(search, limit);
            
        // Then
        assertEquals(expectedArtists, result);
        verify(artistService).lookupArtists(any(LookupRequestDTO.class));
    }
    
    @Test
    void lookupArtists_whenExceptionThrown_shouldThrowDataAccessException() {
        // Given
        String search = "radio";
        String errorMessage = "Test error";
        
        when(artistService.lookupArtists(any(LookupRequestDTO.class)))
            .thenThrow(new RuntimeException(errorMessage));
            
        // When & Then
        DataAccessException exception = assertThrows(DataAccessException.class, () -> 
            artistController.lookupArtists(search, null)
        );
        
        assertEquals("Failed to lookup artists: " + errorMessage, exception.getMessage());
        verify(artistService).lookupArtists(any(LookupRequestDTO.class));
    }
    
    @Test
    void bindToExisting_shouldReturnBoundEntityProjection() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        Long externalId = 1L;
        Long masterId = 101L;
        
        EntityBindToExistingRequestDTO request = EntityBindToExistingRequestDTO.builder()
            .masterId(masterId)
            .build();
        
        TestBoundEntityProjectionImpl projection = new TestBoundEntityProjectionImpl(
            externalId, dataSource, masterId, "Radiohead"
        );
        
        when(artistService.bindToExisting(dataSource, externalId, request))
            .thenReturn(projection);

        // When
        BoundEntityProjection result = artistController.bindToExisting(dataSource, externalId, request);

        // Then
        assertEquals(projection, result);
        verify(artistService).bindToExisting(dataSource, externalId, request);
    }
    
    @Test
    void bindToExisting_whenExceptionThrown_shouldThrowEntityBindingException() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        Long externalId = 1L;
        Long masterId = 101L;
        String errorMessage = "Test error";
        
        EntityBindToExistingRequestDTO request = EntityBindToExistingRequestDTO.builder()
            .masterId(masterId)
            .build();
        
        when(artistService.bindToExisting(dataSource, externalId, request))
            .thenThrow(new RuntimeException(errorMessage));

        // When & Then
        EntityBindingException exception = assertThrows(EntityBindingException.class, () -> 
            artistController.bindToExisting(dataSource, externalId, request)
        );
        
        assertEquals("Failed to bind artist to existing: " + errorMessage, exception.getMessage());
        verify(artistService).bindToExisting(dataSource, externalId, request);
    }
    
    @Test
    void createAndBind_shouldReturnBoundEntityProjection() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        Long externalId = 1L;
        String artistName = "New Artist";
        
        EntityCreateAndBindRequestDTO request = EntityCreateAndBindRequestDTO.builder()
            .entityName(artistName)
            .build();
        
        TestBoundEntityProjectionImpl projection = new TestBoundEntityProjectionImpl(
            externalId, dataSource, 101L, artistName
        );
        
        when(artistService.createAndBind(dataSource, externalId, request))
            .thenReturn(projection);

        // When
        BoundEntityProjection result = artistController.createAndBind(dataSource, externalId, request);

        // Then
        assertEquals(projection, result);
        verify(artistService).createAndBind(dataSource, externalId, request);
    }
    
    @Test
    void createAndBind_whenExceptionThrown_shouldThrowEntityBindingException() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        Long externalId = 1L;
        String artistName = "Test Artist";
        String errorMessage = "Test error";
        
        EntityCreateAndBindRequestDTO request = EntityCreateAndBindRequestDTO.builder()
            .entityName(artistName)
            .build();
        
        when(artistService.createAndBind(dataSource, externalId, request))
            .thenThrow(new RuntimeException(errorMessage));

        // When & Then
        EntityBindingException exception = assertThrows(EntityBindingException.class, () -> 
            artistController.createAndBind(dataSource, externalId, request)
        );
        
        assertEquals("Failed to create and bind artist: " + errorMessage, exception.getMessage());
        verify(artistService).createAndBind(dataSource, externalId, request);
    }
    
    @Test
    void unbindArtist_shouldReturnBoolean() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        Long externalId = 1L;
        
        when(artistService.unbindArtist(dataSource, externalId)).thenReturn(true);

        // When
        boolean result = artistController.unbindArtist(dataSource, externalId);

        // Then
        assertTrue(result);
        verify(artistService).unbindArtist(dataSource, externalId);
    }
    
    @Test
    void unbindArtist_whenExceptionThrown_shouldThrowEntityBindingException() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        Long externalId = 1L;
        String errorMessage = "Test error";

        when(artistService.unbindArtist(dataSource, externalId))
            .thenThrow(new RuntimeException(errorMessage));

        // When & Then
        EntityBindingException exception = assertThrows(EntityBindingException.class, () -> 
            artistController.unbindArtist(dataSource, externalId)
        );
        
        assertEquals("Failed to unbind artist: " + errorMessage, exception.getMessage());
        verify(artistService).unbindArtist(dataSource, externalId);
    }
    
    @Test
    void batchLookupArtists_shouldReturnBatchLookupResponseDTO() {
        // Given
        List<String> searchTerms = List.of("radio", "queen");
        Integer limit = 10;
        
        BaseBatchLookupRequestDTO request = BaseBatchLookupRequestDTO.builder()
            .searchRequests(createLookupRequests(searchTerms))
            .limit(limit)
            .build();
        
        Map<String, List<LookupResultDTO>> resultMap = new HashMap<>();
        resultMap.put("radio", List.of(
            new LookupResultDTO(1L, "Radiohead"),
            new LookupResultDTO(2L, "Radio Moscow")
        ));
        resultMap.put("queen", List.of(
            new LookupResultDTO(3L, "Queen")
        ));
        
        BatchLookupResponseDTO expectedResponse = BatchLookupResponseDTO.builder()
            .results(resultMap)
            .build();
        
        when(artistService.batchLookupArtists(request)).thenReturn(expectedResponse);
        
        // When
        BatchLookupResponseDTO result = artistController.batchLookupArtists(request);
        
        // Then
        assertEquals(expectedResponse, result);
        verify(artistService).batchLookupArtists(request);
    }
    
    @Test
    void batchLookupArtists_whenExceptionThrown_shouldThrowDataAccessException() {
        // Given
        List<String> searchTerms = List.of("radio", "queen");
        Integer limit = 10;
        String errorMessage = "Test error";
        
        BaseBatchLookupRequestDTO request = BaseBatchLookupRequestDTO.builder()
            .searchRequests(createLookupRequests(searchTerms))
            .limit(limit)
            .build();
        
        when(artistService.batchLookupArtists(request))
            .thenThrow(new RuntimeException(errorMessage));
        
        // When & Then
        DataAccessException exception = assertThrows(DataAccessException.class, () -> 
            artistController.batchLookupArtists(request)
        );
        
        assertEquals("Failed to batch lookup artists: " + errorMessage, exception.getMessage());
        verify(artistService).batchLookupArtists(request);
    }
    
    /**
     * Helper method to convert a list of search terms to a list of LookupRequestDTO
     */
    private List<LookupRequestDTO> createLookupRequests(List<String> searchTerms) {
        return searchTerms.stream()
            .map(term -> LookupRequestDTO.builder().search(term).build())
            .collect(Collectors.toList());
    }
}
