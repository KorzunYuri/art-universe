package yurykorzun.art.universe.music.data.raw.lastfm.etl.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import yurykorzun.art.universe.data.raw.common.etl.entity.ApiCallStatus;
import yurykorzun.art.universe.data.raw.common.etl.entity.ApiResponseStatus;
import yurykorzun.art.universe.common.exception.CustomEntityNotFoundException;
import yurykorzun.art.universe.common.exception.DataFetchException;
import yurykorzun.art.universe.common.web.exception.ErrorResponse;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.dto.LastfmApiResponseDto;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.service.LastfmApiResponseService;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.common.LastfmEntityType;
import yurykorzun.art.universe.common.test.archetypes.BaseMvcTest;

import java.time.Instant;
import java.util.Map;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LastfmApiResponseController.class)
class LastfmApiResponseControllerMvcTest extends BaseMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private LastfmApiResponseService apiResponseService;

    @Test
    void GET_apiResponseBody_shouldReturnApiResponseBody_whenFound() throws Exception {
        // Given
        Long responseId = 1L;
        String responseBody = "{\"artist\":{\"name\":\"Test Artist\",\"mbid\":\"test-mbid\"}}";
        JsonNode expectedResponse = objectMapper.readTree(responseBody);

        when(apiResponseService.getApiResponseBody(eq(responseId)))
            .thenReturn(expectedResponse);

        // When & Then
        mockMvc.perform(get("/api/v1/api/responses/{id}/body", responseId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json(responseBody));
    }

    @Test
    void GET_apiResponse_shouldReturnApiResponseDto_whenFound() throws Exception {
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

        String expectedJson = objectMapper.writeValueAsString(expectedDto);

        // When & Then
        mockMvc.perform(get("/api/v1/api/responses/{id}", responseId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json(expectedJson));
    }

    @Test
    void GET_apiResponse_shouldReturnNotFound_whenResponseDoesNotExist() throws Exception {
        // Given
        Long responseId = 999L;
        CustomEntityNotFoundException expectedException = new CustomEntityNotFoundException("response", responseId);
        
        when(apiResponseService.getApiResponseById(eq(responseId)))
            .thenThrow(expectedException);

        String expectedJson = objectMapper.writeValueAsString(new ErrorResponse(expectedException.getMessage()));

        // When & Then
        mockMvc.perform(get("/api/v1/api/responses/{id}", responseId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound())
            .andExpect(content().json(expectedJson));
    }

    @Test
    void GET_apiResponse_shouldReturnError_whenServiceFails() throws Exception {
        // Given
        Long responseId = 1L;
        String errorMessage = "Test exception";
        
        when(apiResponseService.getApiResponseById(eq(responseId)))
            .thenThrow(new DataFetchException(errorMessage));

        String expectedJson = objectMapper.writeValueAsString(new ErrorResponse(errorMessage));

        // When & Then
        mockMvc.perform(get("/api/v1/api/responses/{id}", responseId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isInternalServerError())
            .andExpect(content().json(expectedJson));
    }
}
