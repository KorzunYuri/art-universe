package yurykorzun.art.universe.music.data.master.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yurykorzun.art.universe.music.data.master.dto.BoundEntityProjection;
import yurykorzun.art.universe.music.data.master.dto.TestBoundEntityProjectionImpl;
import yurykorzun.art.universe.music.data.master.entity.DataSource;
import yurykorzun.art.universe.music.data.master.exception.DataAccessException;
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
    void findBoundAlbums_shouldReturnListOfBoundEntityProjections() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        List<Long> externalIds = Arrays.asList(101L, 102L, 999L);
        
        TestBoundEntityProjectionImpl projection = new TestBoundEntityProjectionImpl(
            101L, dataSource, 201L, "Test Album"
        );
        List<BoundEntityProjection> mockBindings = List.of(projection);
        
        when(albumService.findBoundAlbums(dataSource, externalIds))
            .thenReturn(mockBindings);

        // When
        List<BoundEntityProjection> result = albumController.findBoundAlbums(dataSource, externalIds);

        // Then
        assertEquals(mockBindings, result);
        verify(albumService).findBoundAlbums(dataSource, externalIds);
    }

    @Test
    void findBoundAlbums_whenExceptionThrown_shouldThrowDataAccessException() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        List<Long> externalIds = Arrays.asList(101L, 102L);
        String errorMessage = "Test exception";
        
        when(albumService.findBoundAlbums(dataSource, externalIds))
            .thenThrow(new RuntimeException(errorMessage));

        // When & Then
        DataAccessException exception = assertThrows(DataAccessException.class, () -> 
            albumController.findBoundAlbums(dataSource, externalIds)
        );
        
        assertEquals("Failed to get bound albums: " + errorMessage, exception.getMessage());
        verify(albumService).findBoundAlbums(dataSource, externalIds);
    }
    
    @Test
    void findBoundAlbums_withNoResults_shouldReturnEmptyList() {
        // Given
        final DataSource dataSource = DataSource.LASTFM;
        List<BoundEntityProjection> emptyList = Collections.emptyList();
        
        when(albumService.findBoundAlbums(eq(dataSource), any()))
            .thenReturn(emptyList);

        // When
        List<BoundEntityProjection> result = albumController.findBoundAlbums(dataSource, List.of(999L, 888L));

        // Then
        assertEquals(emptyList, result);
        verify(albumService).findBoundAlbums(eq(dataSource), any());
    }
}
