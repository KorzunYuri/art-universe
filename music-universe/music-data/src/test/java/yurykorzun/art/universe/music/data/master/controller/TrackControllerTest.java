package yurykorzun.art.universe.music.data.master.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yurykorzun.art.universe.music.data.master.dto.binding.ArtistRelatedEntityBindToExistingRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.binding.ArtistRelatedEntityCreateAndBindRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.binding.BoundEntityProjection;
import yurykorzun.art.universe.music.data.master.dto.binding.TestBoundEntityProjectionImpl;
import yurykorzun.art.universe.music.data.master.dto.lookup.ArtistRelatedBatchLookupRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.lookup.ArtistRelatedLookupRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.lookup.BatchLookupResponseDTO;
import yurykorzun.art.universe.music.data.master.dto.lookup.LookupResultDTO;
import yurykorzun.art.universe.music.data.master.entity.DataSource;
import yurykorzun.art.universe.music.data.master.exception.DataAccessException;
import yurykorzun.art.universe.music.data.master.exception.EntityBindingException;
import yurykorzun.art.universe.music.data.master.service.TrackService;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TrackControllerTest {

    @Mock
    private TrackService trackService;

    @InjectMocks
    private TrackController trackController;

    @Test
    void findBoundTracks_shouldReturnListOfBoundEntityProjections() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        List<Long> externalIds = Arrays.asList(101L, 102L, 999L);
        
        TestBoundEntityProjectionImpl projection = new TestBoundEntityProjectionImpl(
            101L, dataSource, 201L, "Test Track"
        );
        List<BoundEntityProjection> mockBindings = List.of(projection);
        
        when(trackService.findBoundTracks(dataSource, externalIds))
            .thenReturn(mockBindings);

        // When
        List<BoundEntityProjection> result = trackController.findBoundTracks(dataSource, externalIds);

        // Then
        assertEquals(mockBindings, result);
        verify(trackService).findBoundTracks(dataSource, externalIds);
    }

    @Test
    void findBoundTracks_whenExceptionThrown_shouldThrowDataAccessException() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        List<Long> externalIds = Arrays.asList(101L, 102L);
        String errorMessage = "Test exception";
        
        when(trackService.findBoundTracks(dataSource, externalIds))
            .thenThrow(new RuntimeException(errorMessage));

        // When & Then
        DataAccessException exception = assertThrows(DataAccessException.class, () -> 
            trackController.findBoundTracks(dataSource, externalIds)
        );
        
        assertEquals("Failed to get bound tracks: " + errorMessage, exception.getMessage());
        verify(trackService).findBoundTracks(dataSource, externalIds);
    }
    
    @Test
    void findBoundTracks_withNoResults_shouldReturnEmptyList() {
        // Given
        final DataSource dataSource = DataSource.LASTFM;
        List<BoundEntityProjection> emptyList = Collections.emptyList();
        
        when(trackService.findBoundTracks(eq(dataSource), any()))
            .thenReturn(emptyList);

        // When
        List<BoundEntityProjection> result = trackController.findBoundTracks(dataSource, List.of(999L, 888L));

        // Then
        assertEquals(emptyList, result);
        verify(trackService).findBoundTracks(eq(dataSource), any());
    }

    @Test
    void bindToExisting_shouldReturnBoundEntityProjection() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        Long externalId = 1L;
        Long masterId = 123L;
        Long primaryArtistId = 456L;
        
        ArtistRelatedEntityBindToExistingRequestDTO request = ArtistRelatedEntityBindToExistingRequestDTO.builder()
            .masterId(masterId)
            .primaryArtistId(primaryArtistId)
            .build();
        
        TestBoundEntityProjectionImpl projection = new TestBoundEntityProjectionImpl(
            externalId, dataSource, masterId, "Test Track"
        );
        
        when(trackService.bindToExisting(eq(dataSource), eq(externalId), any(ArtistRelatedEntityBindToExistingRequestDTO.class)))
            .thenReturn(projection);

        // When
        BoundEntityProjection result = trackController.bindToExisting(dataSource, externalId, request);

        // Then
        assertEquals(projection, result);
        verify(trackService).bindToExisting(eq(dataSource), eq(externalId), any(ArtistRelatedEntityBindToExistingRequestDTO.class));
    }
    
    @Test
    void bindToExisting_whenExceptionThrown_shouldThrowEntityBindingException() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        Long externalId = 1L;
        Long masterId = 123L;
        Long primaryArtistId = 456L;
        String errorMessage = "Test error";
        
        ArtistRelatedEntityBindToExistingRequestDTO request = ArtistRelatedEntityBindToExistingRequestDTO.builder()
            .masterId(masterId)
            .primaryArtistId(primaryArtistId)
            .build();
        
        when(trackService.bindToExisting(eq(dataSource), eq(externalId), any(ArtistRelatedEntityBindToExistingRequestDTO.class)))
            .thenThrow(new RuntimeException(errorMessage));

        // When & Then
        EntityBindingException exception = assertThrows(EntityBindingException.class, () -> 
            trackController.bindToExisting(dataSource, externalId, request)
        );
        
        assertEquals("Failed to bind track to existing: " + errorMessage, exception.getMessage());
        verify(trackService).bindToExisting(eq(dataSource), eq(externalId), any(ArtistRelatedEntityBindToExistingRequestDTO.class));
    }
    
    @Test
    void createAndBind_shouldReturnBoundEntityProjection() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        Long externalId = 1L;
        String trackName = "Test Track";
        Long primaryArtistId = 100L;
        
        ArtistRelatedEntityCreateAndBindRequestDTO request = ArtistRelatedEntityCreateAndBindRequestDTO.builder()
            .entityName(trackName)
            .primaryArtistId(primaryArtistId)
            .build();
        
        TestBoundEntityProjectionImpl projection = new TestBoundEntityProjectionImpl(
            externalId, dataSource, 101L, trackName
        );
        
        when(trackService.createAndBind(eq(dataSource), eq(externalId), any(ArtistRelatedEntityCreateAndBindRequestDTO.class)))
            .thenReturn(projection);

        // When
        BoundEntityProjection result = trackController.createAndBind(dataSource, externalId, request);

        // Then
        assertEquals(projection, result);
        verify(trackService).createAndBind(eq(dataSource), eq(externalId), any(ArtistRelatedEntityCreateAndBindRequestDTO.class));
    }
    
    @Test
    void createAndBind_whenExceptionThrown_shouldThrowEntityBindingException() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        Long externalId = 1L;
        String trackName = "Test Track";
        Long primaryArtistId = 100L;
        String errorMessage = "Test error";
        
        ArtistRelatedEntityCreateAndBindRequestDTO request = ArtistRelatedEntityCreateAndBindRequestDTO.builder()
            .entityName(trackName)
            .primaryArtistId(primaryArtistId)
            .build();
        
        when(trackService.createAndBind(eq(dataSource), eq(externalId), any(ArtistRelatedEntityCreateAndBindRequestDTO.class)))
            .thenThrow(new RuntimeException(errorMessage));

        // When & Then
        EntityBindingException exception = assertThrows(EntityBindingException.class, () -> 
            trackController.createAndBind(dataSource, externalId, request)
        );
        
        assertEquals("Failed to create and bind track: " + errorMessage, exception.getMessage());
        verify(trackService).createAndBind(eq(dataSource), eq(externalId), any(ArtistRelatedEntityCreateAndBindRequestDTO.class));
    }
    
    @Test
    void unbindTrack_shouldReturnBoolean() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        Long externalId = 1L;
        
        when(trackService.unbindTrack(dataSource, externalId)).thenReturn(true);

        // When
        boolean result = trackController.unbindTrack(dataSource, externalId);

        // Then
        assertTrue(result);
        verify(trackService).unbindTrack(dataSource, externalId);
    }
    
    @Test
    void unbindTrack_whenExceptionThrown_shouldThrowEntityBindingException() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        Long externalId = 1L;
        String errorMessage = "Test error";

        when(trackService.unbindTrack(dataSource, externalId))
            .thenThrow(new RuntimeException(errorMessage));

        // When & Then
        EntityBindingException exception = assertThrows(EntityBindingException.class, () -> 
            trackController.unbindTrack(dataSource, externalId)
        );
        
        assertEquals("Failed to unbind track: " + errorMessage, exception.getMessage());
        verify(trackService).unbindTrack(dataSource, externalId);
    }
    
    @Test
    void lookupTracks_shouldReturnListOfLookupResults() {
        // Given
        String searchTerm = "paranoid";
        Long artistId = 123L;
        
        LookupResultDTO track1 = new LookupResultDTO(1L, "Radiohead - Paranoid Android");
        LookupResultDTO track2 = new LookupResultDTO(2L, "Black Sabbath - Paranoid");
        List<LookupResultDTO> expectedTracks = List.of(track1, track2);
        
        when(trackService.lookupTracks(any(ArtistRelatedLookupRequestDTO.class)))
            .thenReturn(expectedTracks);
            
        // When
        List<LookupResultDTO> result = trackController.lookupTracks(searchTerm, artistId, null, null);
            
        // Then
        assertEquals(expectedTracks, result);
        verify(trackService).lookupTracks(any(ArtistRelatedLookupRequestDTO.class));
    }
    
    @Test
    void lookupTracks_withLimit_shouldReturnListOfLookupResults() {
        // Given
        String searchTerm = "paranoid";
        Long artistId = 123L;
        Integer limit = 5;
        
        LookupResultDTO track1 = new LookupResultDTO(1L, "Radiohead - Paranoid Android");
        LookupResultDTO track2 = new LookupResultDTO(2L, "Black Sabbath - Paranoid");
        List<LookupResultDTO> expectedTracks = List.of(track1, track2);
        
        when(trackService.lookupTracks(any(ArtistRelatedLookupRequestDTO.class)))
            .thenReturn(expectedTracks);
            
        // When
        List<LookupResultDTO> result = trackController.lookupTracks(searchTerm, artistId, null, limit);
            
        // Then
        assertEquals(expectedTracks, result);
        verify(trackService).lookupTracks(any(ArtistRelatedLookupRequestDTO.class));
    }
    
    @Test
    void lookupTracks_withoutArtistId_shouldReturnListOfLookupResults() {
        // Given
        String searchTerm = "paranoid";
        Long artistId = null;
        
        LookupResultDTO track1 = new LookupResultDTO(1L, "Radiohead - Paranoid Android");
        LookupResultDTO track2 = new LookupResultDTO(2L, "Black Sabbath - Paranoid");
        List<LookupResultDTO> expectedTracks = List.of(track1, track2);
        
        when(trackService.lookupTracks(any(ArtistRelatedLookupRequestDTO.class)))
            .thenReturn(expectedTracks);
            
        // When
        List<LookupResultDTO> result = trackController.lookupTracks(searchTerm, artistId, null, null);
            
        // Then
        assertEquals(expectedTracks, result);
        verify(trackService).lookupTracks(any(ArtistRelatedLookupRequestDTO.class));
    }
    
    @Test
    void lookupTracks_whenExceptionThrown_shouldThrowDataAccessException() {
        // Given
        String searchTerm = "paranoid";
        Long artistId = 123L;
        String errorMessage = "Test error";
        
        when(trackService.lookupTracks(any(ArtistRelatedLookupRequestDTO.class)))
            .thenThrow(new RuntimeException(errorMessage));
            
        // When & Then
        DataAccessException exception = assertThrows(DataAccessException.class, () -> 
            trackController.lookupTracks(searchTerm, artistId, null, null)
        );
        
        assertEquals("Failed to lookup tracks: " + errorMessage, exception.getMessage());
        verify(trackService).lookupTracks(any(ArtistRelatedLookupRequestDTO.class));
    }
    
    @Test
    void batchLookupTracks_shouldReturnBatchLookupResponseDTO() {
        // Given
        List<String> searchTerms = List.of("paranoid", "karma");
        Long artistId = 123L;
        Integer limit = 10;
        
        ArtistRelatedBatchLookupRequestDTO request = ArtistRelatedBatchLookupRequestDTO.builder()
            .searchRequests(createArtistRelatedLookupRequests(searchTerms, artistId))
            .limit(limit)
            .build();
        
        Map<String, List<LookupResultDTO>> resultMap = new HashMap<>();
        resultMap.put("paranoid", List.of(
            new LookupResultDTO(1L, "Radiohead - Paranoid Android"),
            new LookupResultDTO(2L, "Black Sabbath - Paranoid")
        ));
        resultMap.put("karma", List.of(
            new LookupResultDTO(3L, "Radiohead - Karma Police")
        ));
        
        BatchLookupResponseDTO expectedResponse = BatchLookupResponseDTO.builder()
            .results(resultMap)
            .build();
        
        when(trackService.batchLookupTracks(request)).thenReturn(expectedResponse);
        
        // When
        BatchLookupResponseDTO result = trackController.batchLookupTracks(request);
        
        // Then
        assertEquals(expectedResponse, result);
        verify(trackService).batchLookupTracks(request);
    }
    
    @Test
    void batchLookupTracks_whenExceptionThrown_shouldThrowDataAccessException() {
        // Given
        List<String> searchTerms = List.of("paranoid", "karma");
        Long artistId = 123L;
        Integer limit = 10;
        String errorMessage = "Test error";
        
        ArtistRelatedBatchLookupRequestDTO request = ArtistRelatedBatchLookupRequestDTO.builder()
            .searchRequests(createArtistRelatedLookupRequests(searchTerms, artistId))
            .limit(limit)
            .build();
        
        when(trackService.batchLookupTracks(request))
            .thenThrow(new RuntimeException(errorMessage));
        
        // When & Then
        DataAccessException exception = assertThrows(DataAccessException.class, () -> 
            trackController.batchLookupTracks(request)
        );
        
        assertEquals("Failed to batch lookup tracks: " + errorMessage, exception.getMessage());
        verify(trackService).batchLookupTracks(request);
    }
    
    /**
     * Helper method to convert a list of search terms to a list of ArtistRelatedLookupRequestDTO
     */
    private List<ArtistRelatedLookupRequestDTO> createArtistRelatedLookupRequests(List<String> searchTerms, Long artistId) {
        return searchTerms.stream()
            .map(term -> ArtistRelatedLookupRequestDTO.builder()
                .search(term)
                .masterArtistId(artistId)
                .dataSource(DataSource.LASTFM)
                .build())
            .collect(Collectors.toList());
    }
}
