package yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import yurykorzun.art.universe.common.data.raw.api.client.entity.ApiResponseStatus;
import yurykorzun.art.universe.common.exception.EntityNotFoundException;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.dto.LastfmApiResponseCreateRequest;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.dto.LastfmApiResponseDto;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiResponse;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.repository.LastfmApiResponseRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.api.utils.LastfmApiClientResourceUtil;
import yurykorzun.art.universe.music.data.raw.lastfm.common.EntityCreationHelper;
import yurykorzun.art.universe.music.data.raw.lastfm.common.config.TestBeansConfig;

import java.util.Optional;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LastfmApiResponseServiceImplTest {

    @Mock
    private LastfmApiResponseRepository apiResponseRepository;

    private LastfmApiResponseServiceImpl apiResponseService;

    @BeforeEach
    void setUp() {
        apiResponseService = new LastfmApiResponseServiceImpl(
            apiResponseRepository,
            TestBeansConfig.getObjectMapper(),
            null
        );

        // self-inject a spy
        apiResponseService = Mockito.spy(apiResponseService);
        ReflectionTestUtils.setField(apiResponseService, "self", apiResponseService);
    }

    private static final String sampleResponse = LastfmApiClientResourceUtil.getAnyResponse();

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
    void createResponse_shouldCreateApiResponse_whenValidDataProvided() {
        // given
        long id = 1L;
        LastfmApiResponseCreateRequest request = validCreateResponseRequestSupplier().get();
        LastfmApiResponse created = getMockResponse(request, id);
        when(apiResponseRepository.save(any(LastfmApiResponse.class))).thenReturn(created);

        // when
        long returnedId = apiResponseService.createResponse(request);

        // then
        verify(apiResponseRepository).save(any(LastfmApiResponse.class));
        assertEquals(id, returnedId);
    }

    @Test
    void updateStatus_shouldUpdateStatus_whenValidTransitionProvided() {
        // given
        long id = 1L;
        LastfmApiResponse apiResponse = getMockResponse(validCreateResponseRequestSupplier().get(), id);
        when(apiResponseRepository.getReferenceById(id)).thenReturn(apiResponse);

        // when
        apiResponseService.setStatus(id, ApiResponseStatus.PENDING);

        // then
        verify(apiResponseRepository).getReferenceById(id);
        verify(apiResponseRepository).save(apiResponse);
        assertEquals(ApiResponseStatus.PENDING, apiResponse.getStatus());
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
        assertThrows(EntityNotFoundException.class, () -> apiResponseService.getApiResponseById(id));
    }

    @Test
    void getApiResponseBody_shouldReturnApiResponseBody_whenResponseExists() throws JsonProcessingException {
        // given
        long id = 1L;
        LastfmApiResponse response = getMockResponse(validCreateResponseRequestSupplier().get(), id);
        JsonNode expectedJson = TestBeansConfig.getObjectMapper().readTree(response.getResponseBody());
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
        assertThrows(EntityNotFoundException.class, () -> apiResponseService.getApiResponseBody(id));
    }

}