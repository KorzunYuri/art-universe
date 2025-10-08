package yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import yurykorzun.art.universe.common.data.raw.api.client.entity.ApiResponseStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.dto.LastfmApiResponseCreateRequest;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiResponse;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.repository.LastfmApiResponseRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.common.EntityCreationHelper;

import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LastfmApiResponseServiceImplTest {

    @Mock
    private LastfmApiResponseRepository apiResponseRepository;

    private LastfmApiResponseServiceImpl apiResponseService;

    @BeforeEach
    void setUp() {
        apiResponseService = new LastfmApiResponseServiceImpl(
            apiResponseRepository,
            null
        );

        // self-inject a spy
        apiResponseService = Mockito.spy(apiResponseService);
        ReflectionTestUtils.setField(apiResponseService, "self", apiResponseService);
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
    void updateStatus_shouldUpdateStatus_whenValidTransitionProvided() {
        // given
        long id = 1L;
        LastfmApiResponse apiResponse = getMockResponse(validCreateResponseRequestSupplier().get(), id);

        // when
        apiResponseService.setStatus(apiResponse, ApiResponseStatus.PROCESSING);

        // then
        verify(apiResponseRepository).save(apiResponse);
        assertEquals(ApiResponseStatus.PROCESSING, apiResponse.getStatus());
    }
}