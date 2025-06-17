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
import yurykorzun.art.universe.music.data.approved.entity.DataSource;
import yurykorzun.art.universe.music.data.approved.service.TrackService;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TrackControllerTest {

    @Mock
    private TrackService trackService;

    @InjectMocks
    private TrackController trackController;

    @Mock
    private List<BoundEntityProjection> mockBindings;

    @Test
    void whenFindBoundTracks_shouldReturnSuccessResponse() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        List<Long> externalIds = Arrays.asList(101L, 102L, 999L);
        
        when(trackService.findBoundTracks(dataSource, externalIds))
            .thenReturn(mockBindings);

        // When
        ResponseEntity<ResponseWrapper<List<BoundEntityProjection>>> response = 
            trackController.findBoundTracks(dataSource, externalIds);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        assertEquals(mockBindings, response.getBody().getData());
        verify(trackService, times(1)).findBoundTracks(dataSource, externalIds);
    }

    @Test
    void whenFindBoundTracks_withException_shouldReturnFailureResponse() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        List<Long> externalIds = Arrays.asList(101L, 102L);
        
        when(trackService.findBoundTracks(dataSource, externalIds))
            .thenThrow(new RuntimeException("Test exception"));

        // When
        ResponseEntity<ResponseWrapper<List<BoundEntityProjection>>> response = 
            trackController.findBoundTracks(dataSource, externalIds);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertNotNull(response.getBody().getMessage());
        assertTrue(response.getBody().getMessage().contains("Failed to get bound tracks"));
        assertTrue(response.getBody().getMessage().contains("Test exception"));
        assertNull(response.getBody().getData());
    }
}
