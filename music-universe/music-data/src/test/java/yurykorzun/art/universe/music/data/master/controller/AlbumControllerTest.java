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
import yurykorzun.art.universe.music.data.master.entity.DataSource;
import yurykorzun.art.universe.music.data.master.service.AlbumService;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AlbumControllerTest {

    @Mock
    private AlbumService albumService;

    @InjectMocks
    private AlbumController albumController;

    @Test
    void whenFindBoundAlbums_shouldReturnSuccessResponse() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        List<Long> externalIds = Arrays.asList(101L, 102L, 999L);
        
        TestBoundEntityProjectionImpl projection = new TestBoundEntityProjectionImpl(
            101L, dataSource, 201L, "Test Album"
        );
        List<BoundEntityProjection> mockBindings = List.of(projection);
        
        when(albumService.findBoundAlbums(dataSource, externalIds))
            .thenReturn(mockBindings);
            
        ResponseEntity<ResponseWrapper<List<BoundEntityProjection>>> expectedResponse = 
            ResponseWrapper.success(mockBindings);

        // When
        ResponseEntity<ResponseWrapper<List<BoundEntityProjection>>> actualResponse = 
            albumController.findBoundAlbums(dataSource, externalIds);

        // Then
        assertEquals(HttpStatus.OK, actualResponse.getStatusCode());
        assertEquals(expectedResponse, actualResponse);

        verify(albumService).findBoundAlbums(dataSource, externalIds);
    }

    @Test
    void whenFindBoundAlbums_withException_shouldReturnFailureResponse() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        List<Long> externalIds = Arrays.asList(101L, 102L);
        String errorMessage = "Test exception";
        
        when(albumService.findBoundAlbums(dataSource, externalIds))
            .thenThrow(new RuntimeException(errorMessage));
            
        ResponseEntity<ResponseWrapper<List<BoundEntityProjection>>> expectedResponse = 
            ResponseWrapper.failure(String.format("Failed to get bound albums: %s", errorMessage));

        // When
        ResponseEntity<ResponseWrapper<List<BoundEntityProjection>>> actualResponse = 
            albumController.findBoundAlbums(dataSource, externalIds);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, actualResponse.getStatusCode());
        assertEquals(expectedResponse, actualResponse);

        verify(albumService).findBoundAlbums(dataSource, externalIds);
    }
    
    @Test
    void whenFindBoundAlbums_withNoResults_shouldReturnEmptyList() throws Exception {
        // Given
        final DataSource dataSource = DataSource.LASTFM;
        List<BoundEntityProjection> emptyList = Collections.emptyList();
        
        when(albumService.findBoundAlbums(eq(dataSource), any()))
            .thenReturn(emptyList);
            
        ResponseEntity<ResponseWrapper<List<BoundEntityProjection>>> expectedResponse = 
            ResponseWrapper.success(emptyList);

        // When
        ResponseEntity<ResponseWrapper<List<BoundEntityProjection>>> actualResponse = 
            albumController.findBoundAlbums(dataSource, List.of(999L, 888L));

        // Then
        assertEquals(HttpStatus.OK, actualResponse.getStatusCode());
        assertEquals(expectedResponse, actualResponse);

        verify(albumService).findBoundAlbums(eq(dataSource), any());
    }
}
