package yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.impl;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import yurykorzun.art.universe.common.data.raw.api.client.entity.ApiCallStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.dto.LastfmApiCallCreateRequest;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.repository.LastfmApiCallRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.common.archetypes.FullContextTest;

import java.time.Duration;
import java.time.Instant;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class LastfmApiCallServiceImplTest extends FullContextTest {

    @MockitoBean
    private LastfmApiCallRepository apiCallRepository;

    @Autowired
    private LastfmApiCallServiceImpl service;

    private static final Instant dueDttm = Instant.now().plus(Duration.ofDays(1));
    private Supplier<LastfmApiCallCreateRequest> validCreateRequestSupplier() {
        return () -> LastfmApiCallCreateRequest.builder()
                .type(LastfmApiCallType.TAG_TOP_TAGS)
                .dataSnapshotId(1L)
                .dueDttm(dueDttm)
            .build();
    }

    @Test
    void testApiCallCreation() {
        // given
        LastfmApiCallCreateRequest request = validCreateRequestSupplier().get();
        LastfmApiCall apiCall = LastfmApiCall.builder()
                .id(1L)
                .dueDttm(request.getDueDttm())
                .type(request.getType())
            .build();
        when(apiCallRepository.save(any(LastfmApiCall.class))).thenReturn(apiCall);

        // when
        long returnedId = service.createApiCall(request);

        // then
        verify(apiCallRepository).save(any(LastfmApiCall.class));
        assertEquals(1L, returnedId);
    }

    @Test
    void testApiCallStatusUpdate() {
        // given
        long id = 1L;
        LastfmApiCall apiCall = LastfmApiCall.builder()
                .type(LastfmApiCallType.TAG_TOP_TAGS)
                .dueDttm(Instant.now())
            .build();
        when(apiCallRepository.getReferenceById(id)).thenReturn(apiCall);

        // when
        service.setStatus(id, ApiCallStatus.PENDING);

        // then
        verify(apiCallRepository).getReferenceById(id);
        verify(apiCallRepository).save(apiCall);
        assertEquals(ApiCallStatus.PENDING, apiCall.getStatus());
    }
}