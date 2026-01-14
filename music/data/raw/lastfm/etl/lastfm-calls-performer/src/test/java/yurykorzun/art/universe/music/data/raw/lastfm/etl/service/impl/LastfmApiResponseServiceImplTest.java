package yurykorzun.art.universe.music.data.raw.lastfm.etl.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.dto.LastfmApiResponseCreateRequest;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmApiResponse;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.repository.LastfmApiResponseRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.test.common.entity.EntityCreationHelper;
import yurykorzun.art.universe.common.test.config.CommonTestConfig;

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
    void createResponse_shouldSetValidationError_whenInvalidJson() {
        // given
        LastfmApiResponseCreateRequest request = LastfmApiResponseCreateRequest.builder()
                .apiCall(EntityCreationHelper.createApiCall())
                .responseBody("invalid json")
                .build();
        LastfmApiResponse created = getMockResponse(request, 1L);
        when(apiResponseRepository.save(any(LastfmApiResponse.class))).thenReturn(created);

        // when
        apiResponseService.createResponse(request);

        // then
        verify(apiResponseRepository).save(any(LastfmApiResponse.class));
    }

    @Test
    void createResponse_shouldSetErrorResponse_whenApiReturnsError() {
        // given
        LastfmApiResponseCreateRequest request = LastfmApiResponseCreateRequest.builder()
                .apiCall(EntityCreationHelper.createApiCall())
                .responseBody("{\"error\":6,\"message\":\"Invalid parameters\"}")
                .build();
        LastfmApiResponse created = getMockResponse(request, 1L);
        when(apiResponseRepository.save(any(LastfmApiResponse.class))).thenReturn(created);

        // when
        apiResponseService.createResponse(request);

        // then
        verify(apiResponseRepository).save(any(LastfmApiResponse.class));
    }

}
