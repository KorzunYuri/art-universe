package yurykorzun.art.universe.music.data.master.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yurykorzun.art.universe.music.data.master.dto.ArtistBatchLookupRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.ArtistBatchLookupResponseDTO;
import yurykorzun.art.universe.music.data.master.dto.ArtistBindToExistingRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.ArtistCreateAndBindRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.LookupResultDTO;
import yurykorzun.art.universe.music.data.master.dto.BoundEntityProjection;
import yurykorzun.art.universe.music.data.master.dto.TestBoundEntityProjectionImpl;
import yurykorzun.art.universe.music.data.master.entity.DataSource;
import yurykorzun.art.universe.music.data.master.exception.DataAccessException;
import yurykorzun.art.universe.music.data.master.exception.EntityBindingException;
import yurykorzun.art.universe.music.data.master.service.ArtistService;

import java.util.HashMap;
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
        String name = "radio";
        LookupResultDTO artist1 = new LookupResultDTO(1L, "Radiohead");
        LookupResultDTO artist2 = new LookupResultDTO(2L, "Radio Moscow");
        List<LookupResultDTO> expectedArtists = List.of(artist1, artist2);
        
        when(artistService.searchArtistsByName(name)).thenReturn(expectedArtists);
            
        // When
        List<LookupResultDTO> result = artistController.lookupArtists(name, null);
            
        // Then
        assertEquals(expectedArtists, result);
        verify(artistService).searchArtistsByName(name);
    }
    
    @Test
    void lookupArtists_withLimit_shouldReturnListOfLookupResults() {
        // Given
        String name = "radio";
        Integer limit = 5;
        LookupResultDTO artist1 = new LookupResultDTO(1L, "Radiohead");
        LookupResultDTO artist2 = new LookupResultDTO(2L, "Radio Moscow");
        List<LookupResultDTO> expectedArtists = List.of(artist1, artist2);
        
        when(artistService.searchArtistsByName(name, limit)).thenReturn(expectedArtists);
            
        // When
        List<LookupResultDTO> result = artistController.lookupArtists(name, limit);
            
        // Then
        assertEquals(expectedArtists, result);
        verify(artistService).searchArtistsByName(name, limit);
    }
    
    @Test
    void lookupArtists_whenExceptionThrown_shouldThrowDataAccessException() {
        // Given
        String name = "radio";
        String errorMessage = "Test error";
        
        when(artistService.searchArtistsByName(name))
            .thenThrow(new RuntimeException(errorMessage));
            
        // When & Then
        DataAccessException exception = assertThrows(DataAccessException.class, () -> 
            artistController.lookupArtists(name, null)
        );
        
        assertEquals("Failed to lookup artists: " + errorMessage, exception.getMessage());
        verify(artistService).searchArtistsByName(name);
    }
    
    @Test
    void bindToExisting_shouldReturnBoundEntityProjection() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        Long externalId = 1L;
        Long artistId = 101L;
        
        ArtistBindToExistingRequestDTO request = ArtistBindToExistingRequestDTO.builder()
            .artistId(artistId)
            .build();
        
        TestBoundEntityProjectionImpl projection = new TestBoundEntityProjectionImpl(
            externalId, dataSource, artistId, "Radiohead"
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
        Long artistId = 101L;
        String errorMessage = "Test error";
        
        ArtistBindToExistingRequestDTO request = ArtistBindToExistingRequestDTO.builder()
            .artistId(artistId)
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
        
        ArtistCreateAndBindRequestDTO request = ArtistCreateAndBindRequestDTO.builder()
            .name(artistName)
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
        
        ArtistCreateAndBindRequestDTO request = ArtistCreateAndBindRequestDTO.builder()
            .name(artistName)
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
    void batchLookupArtists_shouldReturnArtistBatchLookupResponseDTO() {
        // Given
        List<String> searchTerms = List.of("radio", "queen");
        Integer limit = 10;
        
        ArtistBatchLookupRequestDTO request = ArtistBatchLookupRequestDTO.builder()
            .searchTerms(searchTerms)
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
        
        ArtistBatchLookupResponseDTO expectedResponse = ArtistBatchLookupResponseDTO.builder()
            .results(resultMap)
            .build();
        
        when(artistService.batchLookupArtists(request)).thenReturn(expectedResponse);
        
        // When
        ArtistBatchLookupResponseDTO result = artistController.batchLookupArtists(request);
        
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
        
        ArtistBatchLookupRequestDTO request = ArtistBatchLookupRequestDTO.builder()
            .searchTerms(searchTerms)
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
}
