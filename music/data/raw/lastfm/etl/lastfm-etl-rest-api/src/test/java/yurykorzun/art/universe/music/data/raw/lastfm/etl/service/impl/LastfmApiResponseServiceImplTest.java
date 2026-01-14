package yurykorzun.art.universe.music.data.raw.lastfm.etl.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yurykorzun.art.universe.common.exception.CustomEntityNotFoundException;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.dto.LastfmApiResponseCreateRequest;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.dto.LastfmApiResponseDto;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmApiResponse;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.repository.LastfmApiResponseRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.test.common.entity.EntityCreationHelper;
import yurykorzun.art.universe.common.test.config.CommonTestConfig;

import java.util.Optional;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LastfmApiResponseServiceImplTest {

    @Mock
    private LastfmApiResponseRepository apiResponseRepository;

    private LastfmApiResponseServiceImpl apiResponseService;

    @BeforeEach
    void setUp() {
        apiResponseService = new LastfmApiResponseServiceImpl(
            apiResponseRepository,
            CommonTestConfig.getObjectMapper()
        );
    }

    private static final String sampleResponse = "{}";

    private Supplier<LastfmApiResponseCreateRequest> validCreateResponseRequestSupplier() {
        return () -> LastfmApiResponseCreateRequest.builder()
                .apiCall(EntityCreationHelper.createApiCall())
                .responseBody(sampleResponse)
                .build();
    }

    private static LastfmApiResponse getMockResponse(LastfmApiResponseCreateRequest request, long id) {
        return LastfmApiResponse.builder()
                .id(id)
                .apiCall(request.getApiCall())
                .responseBody(request.getResponseBody())
            .build();
    }

    @Test
    void getApiResponseById_shouldReturnApiResponseDto_whenResponseExists() {
        // given
        long id = 1L;
        LastfmApiResponse response = getMockResponse(validCreateResponseRequestSupplier().get(), id);
        when(apiResponseRepository.findById(id)).thenReturn(Optional.of(response));

        // when
        LastfmApiResponseDto dto = apiResponseService.getApiResponseById(id);

        // then
        assertNotNull(dto);
        assertEquals(id, dto.id());
        assertEquals(response.getStatus(), dto.status());
    }

    @Test
    void getApiResponseById_shouldThrowException_whenResponseDoesNotExist() {
        // given
        long id = 999L;
        when(apiResponseRepository.findById(id)).thenReturn(Optional.empty());

        // when
        assertThrows(CustomEntityNotFoundException.class, () -> apiResponseService.getApiResponseById(id));
    }

    @Test
    void getApiResponseBody_shouldReturnApiResponseBody_whenResponseExists() throws JsonProcessingException {
        // given
        long id = 1L;
        LastfmApiResponse response = getMockResponse(validCreateResponseRequestSupplier().get(), id);
        JsonNode expectedJson = CommonTestConfig.getObjectMapper().readTree(response.getResponseBody());
        when(apiResponseRepository.findById(id)).thenReturn(Optional.of(response));

        // when
        JsonNode actualJson = apiResponseService.getApiResponseBody(id);

        // then
        assertNotNull(actualJson);
        assertEquals(expectedJson, actualJson);
    }

    @Test
    void getApiResponseBody_shouldThrowException_whenResponseDoesNotExist() {
        // given
        long id = 999L;
        when(apiResponseRepository.findById(id)).thenReturn(Optional.empty());

        // when
        assertThrows(CustomEntityNotFoundException.class, () -> apiResponseService.getApiResponseBody(id));
    }

}