package yurykorzun.art.universe.music.data.raw.lastfm.maintenance.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.LastfmEntityType;
import yurykorzun.art.universe.music.data.raw.lastfm.maintenance.dto.MasterBatchUnbindRequestDTO;
import yurykorzun.art.universe.music.data.raw.lastfm.maintenance.dto.MasterBatchUnbindResponseDTO;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MusicDataIntegrationServiceTest {

    @Mock
    private RestClient restClient;

    @Mock
    private RestClient.Builder restClientBuilder;

    @Mock
    private RestClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private RestClient.RequestBodySpec requestBodySpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    @Mock
    private ResponseEntity<MasterBatchUnbindResponseDTO> responseEntity;

    private MusicDataIntegrationService service;

    private static final String MUSIC_DATA_BASE_URL = "localhost:7082";

    @BeforeEach
    void setUp() {
        // Setup RestClient.Builder mock to return our mocked RestClient
        when(restClientBuilder.baseUrl(anyString())).thenReturn(restClientBuilder);
        when(restClientBuilder.build()).thenReturn(restClient);
        
        // Create service with mocked RestClient.Builder
        service = new MusicDataIntegrationService(restClientBuilder, MUSIC_DATA_BASE_URL);
    }

    /**
     * Setup mocks for successful API calls
     */
    private void setupSuccessfulApiCallMocks() {
        when(restClient.method(any())).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any(MediaType.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toEntity(eq(MasterBatchUnbindResponseDTO.class))).thenReturn(responseEntity);
        
        // Mock response body
        MasterBatchUnbindResponseDTO mockResponse = new MasterBatchUnbindResponseDTO();
        when(responseEntity.getBody()).thenReturn(mockResponse);
    }

    /**
     * Setup mocks for API calls that should fail
     */
    private void setupFailingApiCallMocks() {
        when(restClient.method(any())).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any(MediaType.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toEntity(eq(MasterBatchUnbindResponseDTO.class))).thenThrow(new RuntimeException("API Error"));
    }

    @Test
    void unbindEntities_shouldMakeCorrectApiCall_whenValidEntityTypeAndIds() {
        // Given: Valid entity type and IDs
        setupSuccessfulApiCallMocks();
        LastfmEntityType entityType = LastfmEntityType.ARTIST;
        List<Long> entityIds = Arrays.asList(1L, 2L, 3L);

        // When: Unbind entities
        service.unbindEntities(entityType, entityIds);

        // Then: Verify API call was made correctly
        ArgumentCaptor<String> uriCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<MasterBatchUnbindRequestDTO> bodyCaptor = ArgumentCaptor.forClass(MasterBatchUnbindRequestDTO.class);

        verify(requestBodyUriSpec).uri(uriCaptor.capture());
        verify(requestBodySpec).body(bodyCaptor.capture());

        assertEquals("/api/v1/artists/unbind/lastfm/batch", uriCaptor.getValue());
        assertEquals(entityIds, bodyCaptor.getValue().getExternalIds());
    }

    @Test
    void unbindEntities_shouldMapEntityTypesToCorrectPaths() {
        // Given: Different entity types
        setupSuccessfulApiCallMocks();
        List<Long> entityIds = Arrays.asList(1L);

        // When: Unbind entities of different types
        service.unbindEntities(LastfmEntityType.ARTIST, entityIds);
        service.unbindEntities(LastfmEntityType.ALBUM, entityIds);
        service.unbindEntities(LastfmEntityType.TRACK, entityIds);
        service.unbindEntities(LastfmEntityType.TAG, entityIds);

        // Then: Verify correct paths are used
        ArgumentCaptor<String> uriCaptor = ArgumentCaptor.forClass(String.class);
        verify(requestBodyUriSpec, times(4)).uri(uriCaptor.capture());

        List<String> capturedUris = uriCaptor.getAllValues();
        assertTrue(capturedUris.contains("/api/v1/artists/unbind/lastfm/batch"));
        assertTrue(capturedUris.contains("/api/v1/albums/unbind/lastfm/batch"));
        assertTrue(capturedUris.contains("/api/v1/tracks/unbind/lastfm/batch"));
        assertTrue(capturedUris.contains("/api/v1/categories/unbind/lastfm/batch")); // TAG -> categories
    }

    @Test
    void unbindEntities_shouldSkipApiCall_whenEntityIdsIsEmpty() {
        // Given: Empty entity IDs
        LastfmEntityType entityType = LastfmEntityType.ARTIST;
        List<Long> entityIds = Collections.emptyList();

        // When: Unbind entities
        service.unbindEntities(entityType, entityIds);

        // Then: No API call should be made
        verify(requestBodyUriSpec, never()).uri(anyString());
        verify(requestBodySpec, never()).body(any());
    }

    @Test
    void unbindEntities_shouldSkipApiCall_whenEntityIdsIsNull() {
        // Given: Null entity IDs
        LastfmEntityType entityType = LastfmEntityType.ARTIST;
        List<Long> entityIds = null;

        // When: Unbind entities
        service.unbindEntities(entityType, entityIds);

        // Then: No API call should be made
        verify(requestBodyUriSpec, never()).uri(anyString());
        verify(requestBodySpec, never()).body(any());
    }

    @Test
    void unbindEntities_shouldContinueOnApiError() {
        // Given: API call will fail
        setupFailingApiCallMocks();
        LastfmEntityType entityType = LastfmEntityType.ARTIST;
        List<Long> entityIds = Arrays.asList(1L, 2L);

        // When: Unbind entities (should not throw exception)
        assertDoesNotThrow(() -> service.unbindEntities(entityType, entityIds));

        // Then: API call should have been attempted
        verify(requestBodyUriSpec).uri(anyString());
        verify(requestBodySpec).body(any(MasterBatchUnbindRequestDTO.class));
    }

    @Test
    void unbindEntities_shouldSetCorrectContentType() {
        // Given: Valid entity type and IDs
        setupSuccessfulApiCallMocks();
        LastfmEntityType entityType = LastfmEntityType.ARTIST;
        List<Long> entityIds = Arrays.asList(1L);

        // When: Unbind entities
        service.unbindEntities(entityType, entityIds);

        // Then: Verify content type is set correctly
        verify(requestBodySpec).contentType(MediaType.APPLICATION_JSON);
    }
}
