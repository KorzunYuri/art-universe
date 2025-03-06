package yurykorzun.art.universe.music.data.raw.lastfm.apiclient.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yurykorzun.art.universe.common.data.raw.apiclient.entity.ApiCallStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.apiclient.dto.LastfmApiCallCreateRequest;
import yurykorzun.art.universe.music.data.raw.lastfm.apiclient.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.apiclient.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.apiclient.repository.LastfmApiCallRepository;

import java.time.Duration;
import java.time.Instant;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class LastfmApiCallServiceImplTest {

    @Mock
    private LastfmApiCallRepository repository;

    @InjectMocks
    private LastfmApiCallServiceImpl service;

    private static final Instant dueDttm = Instant.now().plus(Duration.ofDays(1));
    private static final Supplier<LastfmApiCallCreateRequest> validCreateRequestSupplier =
            () -> LastfmApiCallCreateRequest.builder()
                        .type(LastfmApiCallType.TAG_TOP_TAGS)
                        .dueDttm(dueDttm)
                    .build();

    @Test
    void testApiCallCreation() {
        // given
        LastfmApiCallCreateRequest request = validCreateRequestSupplier.get();
        LastfmApiCall apiCall = LastfmApiCall.builder()
                .id(1L)
                .dueDttm(request.getDueDttm())
                .type(request.getType())
            .build();
        when(repository.save(any(LastfmApiCall.class))).thenReturn(apiCall);

        // when
        long returnedId = service.create(request);

        // then
        verify(repository).save(any(LastfmApiCall.class));
        assertEquals(1L, returnedId);
    }

    @Test
    void testApiCallStatusUpdate() {
        // given
        long id = 1L;
        LastfmApiCall apiCall = LastfmApiCall.builder()
                .id(id)
                .type(LastfmApiCallType.TAG_TOP_TAGS)
                .dueDttm(Instant.now())
            .build();
        when(repository.getReferenceById(id)).thenReturn(apiCall);

        // when
        service.setStatus(id, ApiCallStatus.SCHEDULED);

        // then
        verify(repository).getReferenceById(id);
        verify(repository).save(apiCall);
        assertEquals(ApiCallStatus.SCHEDULED, apiCall.getStatus());
    }
}