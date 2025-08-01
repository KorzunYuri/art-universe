package yurykorzun.art.universe.music.data.master.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yurykorzun.art.universe.music.data.master.dto.BoundEntityProjection;
import yurykorzun.art.universe.music.data.master.dto.TestBoundEntityProjectionImpl;
import yurykorzun.art.universe.music.data.master.dto.TrackBindToExistingRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.TrackCreateAndBindRequestDTO;
import yurykorzun.art.universe.music.data.master.entity.DataSource;
import yurykorzun.art.universe.music.data.master.exception.DataAccessException;
import yurykorzun.art.universe.music.data.master.exception.EntityBindingException;
import yurykorzun.art.universe.music.data.master.service.TrackService;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
        Long trackId = 123L;
        Long artistExternalId = 100L;
        
        TrackBindToExistingRequestDTO request = TrackBindToExistingRequestDTO.builder()
            .trackId(trackId)
            .artistExternalId(artistExternalId)
            .build();
        
        TestBoundEntityProjectionImpl projection = new TestBoundEntityProjectionImpl(
            externalId, dataSource, trackId, "Test Track"
        );
        
        when(trackService.bindToExisting(eq(dataSource), eq(externalId), any(TrackBindToExistingRequestDTO.class)))
            .thenReturn(projection);

        // When
        BoundEntityProjection result = trackController.bindToExisting(dataSource, externalId, request);

        // Then
        assertEquals(projection, result);
        verify(trackService).bindToExisting(eq(dataSource), eq(externalId), any(TrackBindToExistingRequestDTO.class));
    }
    
    @Test
    void bindToExisting_whenExceptionThrown_shouldThrowEntityBindingException() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        Long externalId = 1L;
        Long trackId = 123L;
        Long artistExternalId = 100L;
        String errorMessage = "Test error";
        
        TrackBindToExistingRequestDTO request = TrackBindToExistingRequestDTO.builder()
            .trackId(trackId)
            .artistExternalId(artistExternalId)
            .build();
        
        when(trackService.bindToExisting(eq(dataSource), eq(externalId), any(TrackBindToExistingRequestDTO.class)))
            .thenThrow(new RuntimeException(errorMessage));

        // When & Then
        EntityBindingException exception = assertThrows(EntityBindingException.class, () -> 
            trackController.bindToExisting(dataSource, externalId, request)
        );
        
        assertEquals("Failed to bind track to existing: " + errorMessage, exception.getMessage());
        verify(trackService).bindToExisting(eq(dataSource), eq(externalId), any(TrackBindToExistingRequestDTO.class));
    }
    
    @Test
    void createAndBind_shouldReturnBoundEntityProjection() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        Long externalId = 1L;
        String trackName = "Test Track";
        Long artistExternalId = 100L;
        
        TrackCreateAndBindRequestDTO request = TrackCreateAndBindRequestDTO.builder()
            .name(trackName)
            .artistExternalId(artistExternalId)
            .build();
        
        TestBoundEntityProjectionImpl projection = new TestBoundEntityProjectionImpl(
            externalId, dataSource, 101L, trackName
        );
        
        when(trackService.createAndBind(eq(dataSource), eq(externalId), any(TrackCreateAndBindRequestDTO.class)))
            .thenReturn(projection);

        // When
        BoundEntityProjection result = trackController.createAndBind(dataSource, externalId, request);

        // Then
        assertEquals(projection, result);
        verify(trackService).createAndBind(eq(dataSource), eq(externalId), any(TrackCreateAndBindRequestDTO.class));
    }
    
    @Test
    void createAndBind_whenExceptionThrown_shouldThrowEntityBindingException() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        Long externalId = 1L;
        String trackName = "Test Track";
        Long artistExternalId = 100L;
        String errorMessage = "Test error";
        
        TrackCreateAndBindRequestDTO request = TrackCreateAndBindRequestDTO.builder()
            .name(trackName)
            .artistExternalId(artistExternalId)
            .build();
        
        when(trackService.createAndBind(eq(dataSource), eq(externalId), any(TrackCreateAndBindRequestDTO.class)))
            .thenThrow(new RuntimeException(errorMessage));

        // When & Then
        EntityBindingException exception = assertThrows(EntityBindingException.class, () -> 
            trackController.createAndBind(dataSource, externalId, request)
        );
        
        assertEquals("Failed to create and bind track: " + errorMessage, exception.getMessage());
        verify(trackService).createAndBind(eq(dataSource), eq(externalId), any(TrackCreateAndBindRequestDTO.class));
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
}
