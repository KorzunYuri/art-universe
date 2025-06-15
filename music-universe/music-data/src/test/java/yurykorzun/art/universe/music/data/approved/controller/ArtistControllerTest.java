package yurykorzun.art.universe.music.data.approved.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import yurykorzun.art.universe.common.controller.ResponseWrapper;
import yurykorzun.art.universe.music.data.approved.dto.BoundEntityProjection;
import yurykorzun.art.universe.music.data.approved.dto.TestBoundEntityProjectionImpl;
import yurykorzun.art.universe.music.data.approved.entity.DataSource;
import yurykorzun.art.universe.music.data.approved.service.ArtistService;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ArtistControllerTest {

    @InjectMocks
    private ArtistController artistController;

    @Mock
    private ArtistService artistService;

    private List<BoundEntityProjection> mockBoundArtists;

    @BeforeEach
    void setUp() {
        mockBoundArtists = Arrays.asList(
            new TestBoundEntityProjectionImpl(123L, DataSource.LASTFM, 321L, "artist1"),
            new TestBoundEntityProjectionImpl(456L, DataSource.SPOTIFY, 654L, "artist2")
        );
    }

    @Test
    void shouldReturnBoundArtistsSuccessfully() {
        // Given
        DataSource dataSource = DataSource.SPOTIFY;
        List<Long> externalIds = Arrays.asList(123L, 456L);
        
        when(artistService.findBoundArtists(dataSource, externalIds))
            .thenReturn(mockBoundArtists);

        // When
        ResponseEntity<ResponseWrapper<List<BoundEntityProjection>>> response = 
            artistController.findBoundArtists(dataSource, externalIds);

        // Then
        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertEquals(mockBoundArtists, response.getBody().getData());
    }

    @Test
    void shouldReturnErrorWhenServiceFails() {
        // Given
        DataSource dataSource = DataSource.SPOTIFY;
        List<Long> externalIds = Arrays.asList(123L, 456L);
        String errorMessage = "Service error occurred";
        
        when(artistService.findBoundArtists(dataSource, externalIds))
            .thenThrow(new RuntimeException(errorMessage));

        // When
        ResponseEntity<ResponseWrapper<List<BoundEntityProjection>>> response = 
            artistController.findBoundArtists(dataSource, externalIds);

        // Then
        assertFalse(response.getStatusCode().is2xxSuccessful());
        assertTrue(response.getBody().getMessage().contains(errorMessage));
    }
}
