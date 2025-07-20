package yurykorzun.art.universe.music.data.master.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import yurykorzun.art.universe.common.controller.ResponseWrapper;
import yurykorzun.art.universe.music.data.master.dto.BoundEntityProjection;
import yurykorzun.art.universe.music.data.master.dto.TestBoundEntityProjectionImpl;
import yurykorzun.art.universe.music.data.master.dto.TrackBindToExistingRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.TrackCreateAndBindRequestDTO;
import yurykorzun.art.universe.music.data.master.entity.DataSource;
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
    void whenFindBoundTracks_shouldReturnSuccessResponse() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        List<Long> externalIds = Arrays.asList(101L, 102L, 999L);
        
        TestBoundEntityProjectionImpl projection = new TestBoundEntityProjectionImpl(
            101L, dataSource, 201L, "Test Track"
        );
        List<BoundEntityProjection> mockBindings = List.of(projection);
        
        when(trackService.findBoundTracks(dataSource, externalIds))
            .thenReturn(mockBindings);
            
        ResponseEntity<ResponseWrapper<List<BoundEntityProjection>>> expectedResponse = 
            ResponseWrapper.success(mockBindings);

        // When
        ResponseEntity<ResponseWrapper<List<BoundEntityProjection>>> actualResponse = 
            trackController.findBoundTracks(dataSource, externalIds);

        // Then
        assertEquals(HttpStatus.OK, actualResponse.getStatusCode());
        assertEquals(expectedResponse, actualResponse);

        verify(trackService).findBoundTracks(dataSource, externalIds);
    }

    @Test
    void whenFindBoundTracks_withException_shouldReturnFailureResponse() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        List<Long> externalIds = Arrays.asList(101L, 102L);
        String errorMessage = "Test exception";
        
        when(trackService.findBoundTracks(dataSource, externalIds))
            .thenThrow(new RuntimeException(errorMessage));
            
        ResponseEntity<ResponseWrapper<List<BoundEntityProjection>>> expectedResponse = 
            ResponseWrapper.failure(String.format("Failed to get bound tracks: %s", errorMessage));

        // When
        ResponseEntity<ResponseWrapper<List<BoundEntityProjection>>> actualResponse = 
            trackController.findBoundTracks(dataSource, externalIds);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, actualResponse.getStatusCode());
        assertEquals(expectedResponse, actualResponse);

        verify(trackService).findBoundTracks(dataSource, externalIds);
    }
    
    @Test
    void whenFindBoundTracks_withNoResults_shouldReturnEmptyList() throws Exception {
        // Given
        final DataSource dataSource = DataSource.LASTFM;
        List<BoundEntityProjection> emptyList = Collections.emptyList();
        
        when(trackService.findBoundTracks(eq(dataSource), any()))
            .thenReturn(emptyList);
            
        ResponseEntity<ResponseWrapper<List<BoundEntityProjection>>> expectedResponse = 
            ResponseWrapper.success(emptyList);

        // When
        ResponseEntity<ResponseWrapper<List<BoundEntityProjection>>> actualResponse = 
            trackController.findBoundTracks(dataSource, List.of(999L, 888L));

        // Then
        assertEquals(HttpStatus.OK, actualResponse.getStatusCode());
        assertEquals(expectedResponse, actualResponse);

        verify(trackService).findBoundTracks(eq(dataSource), any());
    }

    @Test
    void bindToExisting_shouldReturnSuccessResponse() throws Exception {
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
        ResponseEntity<ResponseWrapper<BoundEntityProjection>> expectedResponse = ResponseWrapper.success(projection);
        
        when(trackService.bindToExisting(eq(dataSource), eq(externalId), any(TrackBindToExistingRequestDTO.class)))
            .thenReturn(projection);

        // When
        ResponseEntity<ResponseWrapper<BoundEntityProjection>> actualResponse =
            trackController.bindToExisting(dataSource, externalId, request);

        // Then
        assertEquals(HttpStatus.OK, actualResponse.getStatusCode());
        assertEquals(expectedResponse, actualResponse);
        
        verify(trackService).bindToExisting(eq(dataSource), eq(externalId), any(TrackBindToExistingRequestDTO.class));
    }
    
    @Test
    void bindToExisting_whenExceptionThrown_shouldReturnFailureResponse() throws Exception {
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
        ResponseEntity<ResponseWrapper<BoundEntityProjection>> expectedResponse =
            ResponseWrapper.failure(String.format("Failed to bind track to existing: %s", errorMessage));
        
        when(trackService.bindToExisting(eq(dataSource), eq(externalId), any(TrackBindToExistingRequestDTO.class)))
            .thenThrow(new RuntimeException(errorMessage));

        // When
        ResponseEntity<ResponseWrapper<BoundEntityProjection>> actualResponse =
            trackController.bindToExisting(dataSource, externalId, request);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, actualResponse.getStatusCode());
        assertEquals(expectedResponse, actualResponse);
        
        verify(trackService).bindToExisting(eq(dataSource), eq(externalId), any(TrackBindToExistingRequestDTO.class));
    }
    
    @Test
    void createAndBind_shouldReturnSuccessResponse() throws Exception {
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
        ResponseEntity<ResponseWrapper<BoundEntityProjection>> expectedResponse = ResponseWrapper.success(projection);
        
        when(trackService.createAndBind(eq(dataSource), eq(externalId), any(TrackCreateAndBindRequestDTO.class)))
            .thenReturn(projection);

        // When
        ResponseEntity<ResponseWrapper<BoundEntityProjection>> actualResponse =
            trackController.createAndBind(dataSource, externalId, request);

        // Then
        assertEquals(HttpStatus.OK, actualResponse.getStatusCode());
        assertEquals(expectedResponse, actualResponse);
        
        verify(trackService).createAndBind(eq(dataSource), eq(externalId), any(TrackCreateAndBindRequestDTO.class));
    }
    
    @Test
    void createAndBind_whenExceptionThrown_shouldReturnFailureResponse() throws Exception {
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
        ResponseEntity<ResponseWrapper<BoundEntityProjection>> expectedResponse =
            ResponseWrapper.failure(String.format("Failed to create and bind track: %s", errorMessage));
        
        when(trackService.createAndBind(eq(dataSource), eq(externalId), any(TrackCreateAndBindRequestDTO.class)))
            .thenThrow(new RuntimeException(errorMessage));

        // When
        ResponseEntity<ResponseWrapper<BoundEntityProjection>> actualResponse =
            trackController.createAndBind(dataSource, externalId, request);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, actualResponse.getStatusCode());
        assertEquals(expectedResponse, actualResponse);
        
        verify(trackService).createAndBind(eq(dataSource), eq(externalId), any(TrackCreateAndBindRequestDTO.class));
    }
    
    @Test
    void unbindTrack_shouldReturnSuccessResponse() throws Exception {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        Long externalId = 1L;
        
        when(trackService.unbindTrack(dataSource, externalId)).thenReturn(true);
        ResponseEntity<ResponseWrapper<Boolean>> expectedResponse = ResponseWrapper.success(true);

        // When
        ResponseEntity<ResponseWrapper<Boolean>> actualResponse =
            trackController.unbindTrack(dataSource, externalId);

        // Then
        assertEquals(HttpStatus.OK, actualResponse.getStatusCode());
        assertEquals(expectedResponse, actualResponse);
        
        verify(trackService).unbindTrack(dataSource, externalId);
    }
    
    @Test
    void unbindTrack_whenExceptionThrown_shouldReturnFailureResponse() throws Exception {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        Long externalId = 1L;
        String errorMessage = "Test error";

        when(trackService.unbindTrack(dataSource, externalId))
            .thenThrow(new RuntimeException(errorMessage));
        ResponseEntity<ResponseWrapper<Boolean>> expectedResponse =
            ResponseWrapper.failure(String.format("Failed to unbind track: %s", errorMessage));

        // When
        ResponseEntity<ResponseWrapper<Boolean>> actualResponse =
            trackController.unbindTrack(dataSource, externalId);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, actualResponse.getStatusCode());
        assertEquals(expectedResponse, actualResponse);
        
        verify(trackService).unbindTrack(dataSource, externalId);
    }
}
