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
import yurykorzun.art.universe.common.config.client.ConfigPropertyHolder;
import yurykorzun.art.universe.music.data.raw.lastfm.config.LastfmMaintenanceProperty;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.common.LastfmEntityType;
import yurykorzun.art.universe.music.data.raw.lastfm.maintenance.dto.MasterBatchUnbindRequestDTO;
import yurykorzun.art.universe.music.data.raw.lastfm.maintenance.dto.MasterBatchUnbindResponseDTO;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

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

    @Mock
    private ConfigPropertyHolder configPropertyHolder;

    private MusicDataIntegrationService service;

    private static final String MUSIC_DATA_BASE_URL = "localhost:7082";
    private static final int MUSIC_DATA_UNBIND_BATCH_SIZE = 1000;

    @BeforeEach
    void setUp() {
        // Setup RestClient.Builder mock to return our mocked RestClient
        when(restClientBuilder.baseUrl(anyString())).thenReturn(restClientBuilder);
        when(restClientBuilder.build()).thenReturn(restClient);
        lenient().when(configPropertyHolder.getInt(LastfmMaintenanceProperty.UNBIND_BATCH_SIZE)).thenReturn(MUSIC_DATA_UNBIND_BATCH_SIZE);

        // Create service with mocked RestClient.Builder
        service = new MusicDataIntegrationService(restClientBuilder, MUSIC_DATA_BASE_URL, configPropertyHolder);
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

    @Test
    void unbindEntities_shouldSplitLargeListIntoBatches() {
        // Given
        LastfmEntityType entityType = LastfmEntityType.ARTIST;
        List<Long> entityIds = IntStream.rangeClosed(1, 2500)
            .mapToLong(i -> (long) i)
            .boxed()
            .toList();

        // Mock the REST client chain
        setupSuccessfulApiCallMocks();
        //when(restClient.method(HttpMethod.DELETE)).thenReturn(requestBodyUriSpec);
        //when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        //when(requestBodySpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        //when(requestBodySpec.body(any(MasterBatchUnbindRequestDTO.class))).thenReturn(requestBodySpec);
        //when(requestBodySpec.retrieve()).thenReturn(responseSpec);

        // Create mock response
        MasterBatchUnbindResponseDTO mockResponse = MasterBatchUnbindResponseDTO.builder()
            .totalProcessed(1000)
            .successCount(800)
            .notFoundCount(200)
            .build();

        when(responseSpec.toEntity(MasterBatchUnbindResponseDTO.class))
            .thenReturn(ResponseEntity.ok(mockResponse));

        // When
        service.unbindEntities(entityType, entityIds);

        // Then
        // Verify that the method was called 3 times (2500 / 1000 = 3 batches: 1000, 1000, 500)
        ArgumentCaptor<MasterBatchUnbindRequestDTO> requestCaptor =
            ArgumentCaptor.forClass(MasterBatchUnbindRequestDTO.class);

        verify(requestBodySpec, times(3)).body(requestCaptor.capture());

        List<MasterBatchUnbindRequestDTO> capturedRequests = requestCaptor.getAllValues();

        // Verify batch sizes
        assertEquals(3, capturedRequests.size(), "Should create 3 batches");
        assertEquals(1000, capturedRequests.get(0).getExternalIds().size(), "First batch should have 1000 items");
        assertEquals(1000, capturedRequests.get(1).getExternalIds().size(), "Second batch should have 1000 items");
        assertEquals(500, capturedRequests.get(2).getExternalIds().size(), "Third batch should have 500 items");

        // Verify that all IDs are included and in correct order
        List<Long> allCapturedIds = new ArrayList<>();
        for (MasterBatchUnbindRequestDTO request : capturedRequests) {
            allCapturedIds.addAll(request.getExternalIds());
        }

        assertEquals(entityIds.size(), allCapturedIds.size(), "All entity IDs should be included");
        assertEquals(entityIds, allCapturedIds, "Entity IDs should be in the same order");
    }

    @Test
    void unbindEntities_shouldHandleExactlyMaxBatchSize() {
        // Given
        LastfmEntityType entityType = LastfmEntityType.TRACK;
        List<Long> entityIds = IntStream.rangeClosed(1, 1000)
            .mapToLong(i -> (long) i)
            .boxed()
            .toList();

        // Mock the REST client chain
        setupSuccessfulApiCallMocks();
        //when(restClient.method(HttpMethod.DELETE)).thenReturn(requestBodyUriSpec);
        //when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        //when(requestBodySpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        //when(requestBodySpec.body(any(MasterBatchUnbindRequestDTO.class))).thenReturn(requestBodySpec);
        //when(requestBodySpec.retrieve()).thenReturn(responseSpec);

        MasterBatchUnbindResponseDTO mockResponse = MasterBatchUnbindResponseDTO.builder()
            .totalProcessed(1000)
            .successCount(1000)
            .notFoundCount(0)
            .build();

        when(responseSpec.toEntity(MasterBatchUnbindResponseDTO.class))
            .thenReturn(ResponseEntity.ok(mockResponse));

        // When
        service.unbindEntities(entityType, entityIds);

        // Then
        ArgumentCaptor<MasterBatchUnbindRequestDTO> requestCaptor =
            ArgumentCaptor.forClass(MasterBatchUnbindRequestDTO.class);

        verify(requestBodySpec, times(1)).body(requestCaptor.capture());

        List<MasterBatchUnbindRequestDTO> capturedRequests = requestCaptor.getAllValues();
        assertEquals(1, capturedRequests.size(), "Should create exactly 1 batch");
        assertEquals(1000, capturedRequests.get(0).getExternalIds().size(), "Batch should have exactly 1000 items");
    }

    @Test
    void unbindEntities_shouldHandleSmallList() {
        // Given
        LastfmEntityType entityType = LastfmEntityType.ALBUM;
        List<Long> entityIds = List.of(1L, 2L, 3L, 4L, 5L);

        // Mock the REST client chain
        setupSuccessfulApiCallMocks();
        //when(restClient.method(HttpMethod.DELETE)).thenReturn(requestBodyUriSpec);
        //when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        //when(requestBodySpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        //when(requestBodySpec.body(any(MasterBatchUnbindRequestDTO.class))).thenReturn(requestBodySpec);
        //when(requestBodySpec.retrieve()).thenReturn(responseSpec);

        MasterBatchUnbindResponseDTO mockResponse = MasterBatchUnbindResponseDTO.builder()
            .totalProcessed(5)
            .successCount(5)
            .notFoundCount(0)
            .build();

        when(responseSpec.toEntity(MasterBatchUnbindResponseDTO.class))
            .thenReturn(ResponseEntity.ok(mockResponse));

        // When
        service.unbindEntities(entityType, entityIds);

        // Then
        ArgumentCaptor<MasterBatchUnbindRequestDTO> requestCaptor =
            ArgumentCaptor.forClass(MasterBatchUnbindRequestDTO.class);

        verify(requestBodySpec, times(1)).body(requestCaptor.capture());

        List<MasterBatchUnbindRequestDTO> capturedRequests = requestCaptor.getAllValues();
        assertEquals(1, capturedRequests.size(), "Should create exactly 1 batch");
        assertEquals(5, capturedRequests.get(0).getExternalIds().size(), "Batch should have exactly 5 items");
        assertEquals(entityIds, capturedRequests.get(0).getExternalIds(), "Should contain all original IDs");
    }

    @Test
    void unbindEntities_shouldContinueOnBatchFailure() {
        // Given
        LastfmEntityType entityType = LastfmEntityType.ARTIST;
        List<Long> entityIds = IntStream.rangeClosed(1, 1500)
            .mapToLong(i -> (long) i)
            .boxed()
            .toList();

        // Mock the REST client chain
        setupSuccessfulApiCallMocks();
        //when(restClient.method(HttpMethod.DELETE)).thenReturn(requestBodyUriSpec);
        //when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        //when(requestBodySpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        //when(requestBodySpec.body(any(MasterBatchUnbindRequestDTO.class))).thenReturn(requestBodySpec);
        //when(requestBodySpec.retrieve()).thenReturn(responseSpec);

        // First call succeeds, second fails, third succeeds
        MasterBatchUnbindResponseDTO successResponse = MasterBatchUnbindResponseDTO.builder()
            .totalProcessed(1000)
            .successCount(800)
            .notFoundCount(200)
            .build();

        when(responseSpec.toEntity(MasterBatchUnbindResponseDTO.class))
            .thenReturn(ResponseEntity.ok(successResponse))
            .thenThrow(new RuntimeException("Network error"))
            .thenReturn(ResponseEntity.ok(successResponse));

        // When
        assertDoesNotThrow(() -> service.unbindEntities(entityType, entityIds));

        // Then
        verify(requestBodySpec, times(2)).body(any(MasterBatchUnbindRequestDTO.class));
    }

    @Test
    void unbindEntities_shouldHandleEmptyList() {
        // Given
        LastfmEntityType entityType = LastfmEntityType.TAG;
        List<Long> entityIds = List.of();

        // When
        service.unbindEntities(entityType, entityIds);

        // Then
        verify(restClient, never()).method(any());
    }

    @Test
    void unbindEntities_shouldHandleNullList() {
        // Given
        LastfmEntityType entityType = LastfmEntityType.ARTIST;
        List<Long> entityIds = null;

        // When
        service.unbindEntities(entityType, entityIds);

        // Then
        verify(restClient, never()).method(any());
    }
}
