package yurykorzun.art.universe.music.data.raw.lastfm.api.client.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yurykorzun.art.universe.common.data.raw.api.client.entity.ApiCallStatus;
import yurykorzun.art.universe.common.data.raw.api.client.entity.ApiResponseStatus;
import yurykorzun.art.universe.common.exception.CustomEntityNotFoundException;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.dto.LastfmApiResponseDto;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.LastfmApiResponseService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.common.LastfmEntityType;
import yurykorzun.art.universe.music.data.raw.lastfm.common.config.CommonTestConfig;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LastfmApiResponseControllerTest {

    @Mock
    private LastfmApiResponseService apiResponseService;

    @InjectMocks
    private LastfmApiResponseController controller;

    @Test
    void getApiResponseBody_shouldReturnApiResponseBody_whenFound() throws JsonProcessingException {
        // Given
        Long responseId = 1L;
        String responseBody = "{\"artist\":{\"name\":\"Test Artist\",\"mbid\":\"test-mbid\"}}";
        ObjectMapper objectMapper = CommonTestConfig.getObjectMapper();
        JsonNode expectedResponse = objectMapper.readTree(responseBody);

        when(apiResponseService.getApiResponseBody(eq(responseId)))
            .thenReturn(expectedResponse);

        // When
        JsonNode actualResponse = controller.getApiResponseBody(responseId);

        // Then
        assertEquals(expectedResponse, actualResponse);
    }

    @Test
    void getApiResponse_shouldReturnApiResponseDto_whenFound() {
        // Given
        Long responseId = 1L;

        LastfmApiResponseDto expectedDto = new LastfmApiResponseDto(
            responseId,
            ApiResponseStatus.COMPLETED,
            Instant.parse("2025-08-11T14:00:00Z"),
            Instant.parse("2025-08-11T14:30:00Z"),
            10L,
            LastfmApiCallType.ARTIST_GET_INFO,
            "artist.getInfo",
            ApiCallStatus.SUCCESSFUL,
            Map.of("artist", "Test Artist", "api_key", "test-key"),
            Instant.parse("2025-08-11T14:00:00Z"),
            Instant.parse("2025-08-11T13:00:00Z"),
            Instant.parse("2025-08-11T14:30:00Z"),
            5L,
            LastfmEntityType.ARTIST,
            100L
        );
        
        when(apiResponseService.getApiResponseById(eq(responseId)))
            .thenReturn(expectedDto);

        // When
        LastfmApiResponseDto result = controller.getApiResponse(responseId);

        // Then
        assertNotNull(result);
        assertEquals(responseId, result.id());
        assertEquals(ApiResponseStatus.COMPLETED, result.status());
        assertEquals(10L, result.apiCallId());
        assertEquals(LastfmApiCallType.ARTIST_GET_INFO, result.apiCallType());
        assertEquals("artist.getInfo", result.apiCallMethod());
        assertEquals(ApiCallStatus.SUCCESSFUL, result.apiCallStatus());
        assertEquals(Map.of("artist", "Test Artist", "api_key", "test-key"), result.apiCallParams());
        assertEquals(5L, result.dataSnapshotId());
        assertEquals(LastfmEntityType.ARTIST, result.entityType());
        assertEquals(100L, result.entityId());
    }

    @Test
    void getApiResponse_shouldThrowCustomEntityNotFoundException_whenResponseNotFound() {
        // Given
        Long responseId = 999L;
        CustomEntityNotFoundException expectedException = new CustomEntityNotFoundException("response", responseId);
        
        when(apiResponseService.getApiResponseById(eq(responseId)))
            .thenThrow(expectedException);

        // When & Then
        CustomEntityNotFoundException exception = assertThrows(CustomEntityNotFoundException.class, () -> {
            controller.getApiResponse(responseId);
        });
        
        assertEquals(expectedException, exception);
    }

    @Test
    void getApiResponse_shouldPropagateServiceException() {
        // Given
        Long responseId = 1L;
        RuntimeException serviceException = new RuntimeException("Database connection error");
        
        when(apiResponseService.getApiResponseById(eq(responseId)))
            .thenThrow(serviceException);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            controller.getApiResponse(responseId);
        });
        
        assertEquals(serviceException, exception);
    }
}
