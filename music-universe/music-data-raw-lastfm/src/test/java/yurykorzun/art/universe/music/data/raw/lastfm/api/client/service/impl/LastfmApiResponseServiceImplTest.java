package yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.impl;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import yurykorzun.art.universe.common.data.raw.api.client.entity.ApiResponseStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.dto.LastfmApiResponseCreateRequest;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiResponse;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.repository.LastfmApiResponseRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.api.utils.LastfmApiClientResourceUtil;
import yurykorzun.art.universe.music.data.raw.lastfm.common.archetypes.FullContextTest;

import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class LastfmApiResponseServiceImplTest extends FullContextTest {

    @MockitoBean
    private LastfmApiResponseRepository apiResponseRepository;

    @Autowired
    private LastfmApiResponseServiceImpl apiResponseService;

    private static final String sampleResponse = LastfmApiClientResourceUtil.getAnyResponse();
    private static final Supplier<LastfmApiResponseCreateRequest> validCreateRequestSupplier =
            () -> LastfmApiResponseCreateRequest.builder()
                    .apiCallId(1L)
                    .apiCallType(LastfmApiCallType.TAG_TOP_TAGS)
                    .responseBody(sampleResponse)
                .build();

    private static LastfmApiResponse getMockResponse(LastfmApiResponseCreateRequest request, long id) {
        return LastfmApiResponse.builder()
                .id(id)
                .apiCallType(request.getApiCallType())
                .apiCallId(request.getApiCallId())
                .responseBody(request.getResponseBody())
            .build();
    }

    @Test
    void testApiCallCreation() {
        // given
        long id = 1L;
        LastfmApiResponseCreateRequest request = validCreateRequestSupplier.get();
        LastfmApiResponse created = getMockResponse(request, id);
        when(apiResponseRepository.save(any(LastfmApiResponse.class))).thenReturn(created);

        // when
        long returnedId = apiResponseService.create(request);

        // then
        verify(apiResponseRepository).save(any(LastfmApiResponse.class));
        assertEquals(id, returnedId);
    }

    @Test
    void testApiCallStatusUpdate() {
        // given
        long id = 1L;
        LastfmApiResponse apiResponse = getMockResponse(validCreateRequestSupplier.get(), id);
        when(apiResponseRepository.getReferenceById(id)).thenReturn(apiResponse);

        // when
        apiResponseService.setStatus(id, ApiResponseStatus.PENDING);

        // then
        verify(apiResponseRepository).getReferenceById(id);
        verify(apiResponseRepository).save(apiResponse);
        assertEquals(ApiResponseStatus.PENDING, apiResponse.getStatus());
    }

}