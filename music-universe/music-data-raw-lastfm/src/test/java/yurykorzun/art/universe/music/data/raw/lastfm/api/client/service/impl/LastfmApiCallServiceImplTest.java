package yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import yurykorzun.art.universe.common.data.raw.api.client.entity.ApiCallStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.dto.LastfmApiCallCreateRequest;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.repository.LastfmApiCallRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.LastfmApiCallPrioritizer;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.LastfmApiClient;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.LastfmApiResponseService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.LastfmEntityType;

import java.time.Duration;
import java.time.Instant;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LastfmApiCallServiceImplTest {

    @Mock
    private LastfmApiCallRepository apiCallRepository;
    @Mock
    private LastfmApiResponseService apiResponseService;
    @Mock
    private LastfmApiCallPrioritizer apiCallPrioritizer;
    @Mock
    private LastfmApiClient apiClient;

    private LastfmApiCallServiceImpl service;

    @BeforeEach
    void setUp() {
        // first create service without self-proxy
        LastfmApiCallServiceImpl rawService =
            new LastfmApiCallServiceImpl(
                apiCallRepository,
                apiResponseService,
                apiCallPrioritizer,
                apiClient,
                null,   // self
                5.0 // limiter constant
            );

        // self-inject a spy
        service = Mockito.spy(rawService);
        ReflectionTestUtils.setField(service, "self", service);
    }

    private static final Instant dueDttm = Instant.now().plus(Duration.ofDays(1));
    private Supplier<LastfmApiCallCreateRequest> validCreateRequestSupplier() {
        return () -> LastfmApiCallCreateRequest.builder()
                .type(LastfmApiCallType.TAG_TOP_TAGS)
                .dataSnapshotId(1L)
                .entityType(LastfmEntityType.TAG)
                .dueDttm(dueDttm)
            .build();
    }

    @Test
    void create_shouldCreateApiCall_whenValidDataProvided() {
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
    void updateStatus_shouldUpdateStatus_whenValidTransitionProvided() {
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