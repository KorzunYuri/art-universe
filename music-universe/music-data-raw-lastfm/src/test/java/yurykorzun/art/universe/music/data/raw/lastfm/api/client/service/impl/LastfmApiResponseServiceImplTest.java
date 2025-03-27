package yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.impl;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import yurykorzun.art.universe.common.data.raw.api.client.entity.ApiResponseStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.dto.LastfmApiResponseCreateRequest;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiResponse;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmDataSnapshot;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.repository.LastfmApiCallRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.repository.LastfmApiResponseRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.repository.LastfmDataSnapshotRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.api.utils.LastfmApiClientResourceUtil;
import yurykorzun.art.universe.music.data.raw.lastfm.common.archetypes.FullContextTest;

import java.time.Instant;
import java.util.Date;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class LastfmApiResponseServiceImplTest extends FullContextTest {

    @MockitoBean
    private LastfmApiResponseRepository apiResponseRepository;

    @Autowired
    private LastfmApiResponseServiceImpl apiResponseService;

    @Autowired
    private LastfmApiCallRepository apiCallRepository;

    @Autowired
    private LastfmDataSnapshotRepository snapshotRepository;

    private static final String sampleResponse = LastfmApiClientResourceUtil.getAnyResponse();

    private LastfmDataSnapshot createDummyDataSnapshot() {
        return snapshotRepository.save(new LastfmDataSnapshot(LastfmApiCallType.TAG_TOP_TAGS, new Date()));
    }

    private LastfmApiCall createDummyApiCall() {
        LastfmDataSnapshot snapshot = createDummyDataSnapshot();
        LastfmApiCall dummyApiCall = LastfmApiCall.builder()
                .type(LastfmApiCallType.TAG_TOP_TAGS)
                .dataSnapshotId(snapshot.getId())
                .dueDttm(Instant.now())
                .build();
        dummyApiCall = apiCallRepository.save(dummyApiCall);
        return dummyApiCall;
    }

    private Supplier<LastfmApiResponseCreateRequest> validCreateRequestSupplier() {
        return () -> LastfmApiResponseCreateRequest.builder()
                .apiCall(createDummyApiCall())
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
    void testApiCallCreation() {
        // given
        long id = 1L;
        LastfmApiResponseCreateRequest request = validCreateRequestSupplier().get();
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
        LastfmApiResponse apiResponse = getMockResponse(validCreateRequestSupplier().get(), id);
        when(apiResponseRepository.getReferenceById(id)).thenReturn(apiResponse);

        // when
        apiResponseService.setStatus(id, ApiResponseStatus.PENDING);

        // then
        verify(apiResponseRepository).getReferenceById(id);
        verify(apiResponseRepository).save(apiResponse);
        assertEquals(ApiResponseStatus.PENDING, apiResponse.getStatus());
    }

}