package yurykorzun.art.universe.music.data.approved.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import yurykorzun.art.universe.common.controller.ResponseWrapper;
import yurykorzun.art.universe.music.data.approved.dto.BoundEntityProjection;
import yurykorzun.art.universe.music.data.approved.dto.TestBoundEntityProjectionImpl;
import yurykorzun.art.universe.music.data.approved.entity.DataSource;
import yurykorzun.art.universe.music.data.approved.service.TrackService;

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
}
