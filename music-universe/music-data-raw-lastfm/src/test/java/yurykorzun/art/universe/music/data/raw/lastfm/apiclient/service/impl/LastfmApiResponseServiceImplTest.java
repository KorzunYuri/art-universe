package yurykorzun.art.universe.music.data.raw.lastfm.apiclient.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yurykorzun.art.universe.common.data.raw.apiclient.entity.ApiResponseStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.apiclient.dto.LastfmApiResponseCreateRequest;
import yurykorzun.art.universe.music.data.raw.lastfm.apiclient.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.apiclient.entity.LastfmApiResponse;
import yurykorzun.art.universe.music.data.raw.lastfm.apiclient.repository.LastfmApiResponseRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.apiclient.utils.LastfmApiClientResourceUtil;

import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LastfmApiResponseServiceImplTest {

    @Mock
    private LastfmApiResponseRepository repository;

    @InjectMocks
    private LastfmApiResponseServiceImpl service;

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
        when(repository.save(any(LastfmApiResponse.class))).thenReturn(created);

        // when
        long returnedId = service.create(request);

        // then
        verify(repository).save(any(LastfmApiResponse.class));
        assertEquals(id, returnedId);
    }

    @Test
    void testApiCallStatusUpdate() {
        // given
        long id = 1L;
        LastfmApiResponse apiResponse = getMockResponse(validCreateRequestSupplier.get(), id);
        when(repository.getReferenceById(id)).thenReturn(apiResponse);

        // when
        service.setStatus(id, ApiResponseStatus.PENDING);

        // then
        verify(repository).getReferenceById(id);
        verify(repository).save(apiResponse);
        assertEquals(ApiResponseStatus.PENDING, apiResponse.getStatus());
    }

}