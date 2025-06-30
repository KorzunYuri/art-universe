package yurykorzun.art.universe.music.data.approved.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import yurykorzun.art.universe.common.controller.ResponseWrapper;
import yurykorzun.art.universe.music.data.approved.dto.ArtistBindingRequestDTO;
import yurykorzun.art.universe.music.data.approved.dto.LookupResultDTO;
import yurykorzun.art.universe.music.data.approved.dto.BoundEntityProjection;
import yurykorzun.art.universe.music.data.approved.dto.TestBoundEntityProjectionImpl;
import yurykorzun.art.universe.music.data.approved.entity.DataSource;
import yurykorzun.art.universe.music.data.approved.service.ArtistService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ArtistControllerTest {

    @Mock
    private ArtistService artistService;

    @InjectMocks
    private ArtistController artistController;

    @Test
    void findBoundArtists_shouldReturnSuccessResponse() throws Exception {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        List<Long> externalIds = List.of(1L, 2L);
        
        TestBoundEntityProjectionImpl projection = new TestBoundEntityProjectionImpl(
            1L, dataSource, 101L, "Test Artist"
        );

        List<BoundEntityProjection> expectedBindings = List.of(projection);
        when(artistService.findBoundArtists(dataSource, externalIds))
            .thenReturn(expectedBindings);
        ResponseEntity<ResponseWrapper<List<BoundEntityProjection>>> expectedResponse =
            ResponseWrapper.success(expectedBindings);

        // When
        ResponseEntity<ResponseWrapper<List<BoundEntityProjection>>> actualResponse =
            artistController.findBoundArtists(dataSource, externalIds);

        // Then
        assertEquals(HttpStatus.OK, actualResponse.getStatusCode());
        assertEquals(expectedResponse, actualResponse);

        verify(artistService).findBoundArtists(dataSource, externalIds);
    }
    
    @Test
    void findBoundArtists_whenExceptionThrown_shouldReturnFailureResponse() throws Exception {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        List<Long> externalIds = List.of(1L, 2L);
        String errorMessage = "Test error";
        
        when(artistService.findBoundArtists(dataSource, externalIds))
            .thenThrow(new RuntimeException(errorMessage));
        ResponseEntity<ResponseWrapper<List<BoundEntityProjection>>> expectedResponse =
            ResponseWrapper.failure(String.format("Failed to get bound artists: %s", errorMessage));

        // When
        ResponseEntity<ResponseWrapper<List<BoundEntityProjection>>> actualResponse =
            artistController.findBoundArtists(dataSource, externalIds);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, actualResponse.getStatusCode());
        assertEquals(expectedResponse, actualResponse);

        verify(artistService).findBoundArtists(dataSource, externalIds);
    }
    
    @Test
    void lookupArtists_shouldReturnSuccessResponse() throws Exception {
        // Given
        String name = "radio";
        LookupResultDTO artist1 = new LookupResultDTO(1L, "Radiohead");
        LookupResultDTO artist2 = new LookupResultDTO(2L, "Radio Moscow");
        List<LookupResultDTO> expectedArtists = List.of(artist1, artist2);
        
        when(artistService.searchArtistsByName(name)).thenReturn(expectedArtists);
        ResponseEntity<ResponseWrapper<List<LookupResultDTO>>> expectedResponse =
            ResponseWrapper.success(expectedArtists);
            
        // When
        ResponseEntity<ResponseWrapper<List<LookupResultDTO>>> actualResponse =
            artistController.lookupArtists(name, null);
            
        // Then
        assertEquals(HttpStatus.OK, actualResponse.getStatusCode());
        assertEquals(expectedResponse, actualResponse);
        
        verify(artistService).searchArtistsByName(name);
    }
    
    @Test
    void lookupArtists_withLimit_shouldReturnSuccessResponse() throws Exception {
        // Given
        String name = "radio";
        Integer limit = 5;
        LookupResultDTO artist1 = new LookupResultDTO(1L, "Radiohead");
        LookupResultDTO artist2 = new LookupResultDTO(2L, "Radio Moscow");
        List<LookupResultDTO> expectedArtists = List.of(artist1, artist2);
        
        when(artistService.searchArtistsByName(name, limit)).thenReturn(expectedArtists);
        ResponseEntity<ResponseWrapper<List<LookupResultDTO>>> expectedResponse =
            ResponseWrapper.success(expectedArtists);
            
        // When
        ResponseEntity<ResponseWrapper<List<LookupResultDTO>>> actualResponse =
            artistController.lookupArtists(name, limit);
            
        // Then
        assertEquals(HttpStatus.OK, actualResponse.getStatusCode());
        assertEquals(expectedResponse, actualResponse);
        
        verify(artistService).searchArtistsByName(name, limit);
    }
    
    @Test
    void lookupArtists_whenExceptionThrown_shouldReturnFailureResponse() throws Exception {
        // Given
        String name = "radio";
        String errorMessage = "Test error";
        
        when(artistService.searchArtistsByName(name))
            .thenThrow(new RuntimeException(errorMessage));
        ResponseEntity<ResponseWrapper<List<LookupResultDTO>>> expectedResponse =
            ResponseWrapper.failure(String.format("Failed to lookup artists: %s", errorMessage));
            
        // When
        ResponseEntity<ResponseWrapper<List<LookupResultDTO>>> actualResponse =
            artistController.lookupArtists(name, null);
            
        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, actualResponse.getStatusCode());
        assertEquals(expectedResponse, actualResponse);
        
        verify(artistService).searchArtistsByName(name);
    }
    
    @Test
    void bindArtist_shouldReturnSuccessResponse() throws Exception {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        Long externalId = 1L;
        String artistName = "Test Artist";
        
        ArtistBindingRequestDTO request = ArtistBindingRequestDTO.builder()
            .name(artistName)
            .build();
        
        TestBoundEntityProjectionImpl projection = new TestBoundEntityProjectionImpl(
            externalId, dataSource, 101L, artistName
        );
        ResponseEntity<ResponseWrapper<BoundEntityProjection>> expectedResponse = ResponseWrapper.success(projection);
        
        when(artistService.bindArtist(eq(dataSource), eq(externalId), any(ArtistBindingRequestDTO.class)))
            .thenReturn(projection);

        // When
        ResponseEntity<ResponseWrapper<BoundEntityProjection>> actualResponse =
            artistController.bindArtist(dataSource, externalId, request);

        // Then
        assertEquals(HttpStatus.OK, actualResponse.getStatusCode());
        assertEquals(expectedResponse, actualResponse);
        
        verify(artistService).bindArtist(eq(dataSource), eq(externalId), any(ArtistBindingRequestDTO.class));
    }
    
    @Test
    void bindArtist_whenExceptionThrown_shouldReturnFailureResponse() throws Exception {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        Long externalId = 1L;
        String artistName = "Test Artist";
        String errorMessage = "Test error";
        
        ArtistBindingRequestDTO request = ArtistBindingRequestDTO.builder()
            .name(artistName)
            .build();
        ResponseEntity<ResponseWrapper<BoundEntityProjection>> expectedResponse =
            ResponseWrapper.failure(String.format("Failed to bind artist: %s", errorMessage));
        
        when(artistService.bindArtist(eq(dataSource), eq(externalId), any(ArtistBindingRequestDTO.class)))
            .thenThrow(new RuntimeException(errorMessage));

        // When
        ResponseEntity<ResponseWrapper<BoundEntityProjection>> actualResponse =
            artistController.bindArtist(dataSource, externalId, request);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, actualResponse.getStatusCode());
        assertEquals(expectedResponse, actualResponse);
        
        verify(artistService).bindArtist(eq(dataSource), eq(externalId), any(ArtistBindingRequestDTO.class));
    }
    
    @Test
    void unbindArtist_shouldReturnSuccessResponse() throws Exception {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        Long externalId = 1L;
        
        when(artistService.unbindArtist(dataSource, externalId)).thenReturn(true);
        ResponseEntity<ResponseWrapper<Boolean>> expectedResponse = ResponseWrapper.success(true);

            // When
        ResponseEntity<ResponseWrapper<Boolean>> actualResponse =
            artistController.unbindArtist(dataSource, externalId);

        // Then
        assertEquals(HttpStatus.OK, actualResponse.getStatusCode());
        assertEquals(expectedResponse, actualResponse);
        
        verify(artistService).unbindArtist(dataSource, externalId);
    }
    
    @Test
    void unbindArtist_whenExceptionThrown_shouldReturnFailureResponse() throws Exception {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        Long externalId = 1L;
        String errorMessage = "Test error";

        when(artistService.unbindArtist(dataSource, externalId))
            .thenThrow(new RuntimeException(errorMessage));
        ResponseEntity<ResponseWrapper<Boolean>> expectedResponse =
            ResponseWrapper.failure(String.format("Failed to unbind artist: %s", errorMessage));

        // When
        ResponseEntity<ResponseWrapper<Boolean>> actualResponse =
            artistController.unbindArtist(dataSource, externalId);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, actualResponse.getStatusCode());
        assertEquals(expectedResponse, actualResponse);
        
        verify(artistService).unbindArtist(dataSource, externalId);
    }
}
