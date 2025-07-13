package yurykorzun.art.universe.music.data.approved.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import yurykorzun.art.universe.common.controller.ResponseWrapper;
import yurykorzun.art.universe.music.data.approved.dto.ArtistBatchLookupRequestDTO;
import yurykorzun.art.universe.music.data.approved.dto.ArtistBatchLookupResponseDTO;
import yurykorzun.art.universe.music.data.approved.dto.ArtistBindToExistingRequestDTO;
import yurykorzun.art.universe.music.data.approved.dto.ArtistCreateAndBindRequestDTO;
import yurykorzun.art.universe.music.data.approved.dto.LookupResultDTO;
import yurykorzun.art.universe.music.data.approved.dto.BoundEntityProjection;
import yurykorzun.art.universe.music.data.approved.dto.TestBoundEntityProjectionImpl;
import yurykorzun.art.universe.music.data.approved.entity.DataSource;
import yurykorzun.art.universe.music.data.approved.service.ArtistService;

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
    void bindToExisting_shouldReturnSuccessResponse() throws Exception {
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
        ResponseEntity<ResponseWrapper<BoundEntityProjection>> expectedResponse = 
            ResponseWrapper.success(projection);
        
        when(artistService.bindToExisting(dataSource, externalId, request))
            .thenReturn(projection);

        // When
        ResponseEntity<ResponseWrapper<BoundEntityProjection>> actualResponse =
            artistController.bindToExisting(dataSource, externalId, request);

        // Then
        assertEquals(HttpStatus.OK, actualResponse.getStatusCode());
        assertEquals(expectedResponse, actualResponse);
        
        verify(artistService).bindToExisting(dataSource, externalId, request);
    }
    
    @Test
    void bindToExisting_whenExceptionThrown_shouldReturnFailureResponse() throws Exception {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        Long externalId = 1L;
        Long artistId = 101L;
        String errorMessage = "Test error";
        
        ArtistBindToExistingRequestDTO request = ArtistBindToExistingRequestDTO.builder()
            .artistId(artistId)
            .build();
        ResponseEntity<ResponseWrapper<BoundEntityProjection>> expectedResponse =
            ResponseWrapper.failure(String.format("Failed to bind artist to existing: %s", errorMessage));
        
        when(artistService.bindToExisting(dataSource, externalId, request))
            .thenThrow(new RuntimeException(errorMessage));

        // When
        ResponseEntity<ResponseWrapper<BoundEntityProjection>> actualResponse =
            artistController.bindToExisting(dataSource, externalId, request);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, actualResponse.getStatusCode());
        assertEquals(expectedResponse, actualResponse);
        
        verify(artistService).bindToExisting(dataSource, externalId, request);
    }
    
    @Test
    void createAndBind_shouldReturnSuccessResponse() throws Exception {
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
        ResponseEntity<ResponseWrapper<BoundEntityProjection>> expectedResponse = 
            ResponseWrapper.success(projection);
        
        when(artistService.createAndBind(dataSource, externalId, request))
            .thenReturn(projection);

        // When
        ResponseEntity<ResponseWrapper<BoundEntityProjection>> actualResponse =
            artistController.createAndBind(dataSource, externalId, request);

        // Then
        assertEquals(HttpStatus.OK, actualResponse.getStatusCode());
        assertEquals(expectedResponse, actualResponse);
        
        verify(artistService).createAndBind(dataSource, externalId, request);
    }
    
    @Test
    void bindArtist_whenExceptionThrown_shouldReturnFailureResponse() throws Exception {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        Long externalId = 1L;
        String artistName = "Test Artist";
        String errorMessage = "Test error";
        
        ArtistCreateAndBindRequestDTO request = ArtistCreateAndBindRequestDTO.builder()
            .name(artistName)
            .build();
        ResponseEntity<ResponseWrapper<BoundEntityProjection>> expectedResponse =
            ResponseWrapper.failure(String.format("Failed to create and bind artist: %s", errorMessage));
        
        when(artistService.createAndBind(dataSource, externalId, request))
            .thenThrow(new RuntimeException(errorMessage));

        // When
        ResponseEntity<ResponseWrapper<BoundEntityProjection>> actualResponse =
            artistController.createAndBind(dataSource, externalId, request);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, actualResponse.getStatusCode());
        assertEquals(expectedResponse, actualResponse);
        
        verify(artistService).createAndBind(dataSource, externalId, request);
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
    
    @Test
    void batchLookupArtists_shouldReturnSuccessResponse() {
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
        ResponseEntity<ResponseWrapper<ArtistBatchLookupResponseDTO>> actualResponse =
            artistController.batchLookupArtists(request);
        
        // Then
        assertEquals(HttpStatus.OK, actualResponse.getStatusCode());
        assertEquals(ResponseWrapper.successBody(expectedResponse), actualResponse.getBody());
        
        verify(artistService).batchLookupArtists(request);
    }
    
    @Test
    void batchLookupArtists_whenExceptionThrown_shouldReturnFailureResponse() {
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
        
        ResponseEntity<ResponseWrapper<ArtistBatchLookupResponseDTO>> expectedResponse =
            ResponseWrapper.failure(String.format("Failed to batch lookup artists: %s", errorMessage));
        
        // When
        ResponseEntity<ResponseWrapper<ArtistBatchLookupResponseDTO>> actualResponse =
            artistController.batchLookupArtists(request);
        
        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, actualResponse.getStatusCode());
        assertEquals(expectedResponse.getBody().isSuccess(), actualResponse.getBody().isSuccess());
        assertEquals(expectedResponse.getBody().getMessage(), actualResponse.getBody().getMessage());
        
        verify(artistService).batchLookupArtists(request);
    }
}
