package yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import yurykorzun.art.universe.common.data.raw.api.client.entity.ApiCallStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.repository.LastfmApiCallRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.LastfmApiClient;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.LastfmApiResponseService;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

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
    private LastfmApiClient apiClient;

    private LastfmApiCallServiceImpl service;

    @BeforeEach
    void setUp() {
        // first create service without self-proxy
        LastfmApiCallServiceImpl rawService =
            new LastfmApiCallServiceImpl(
                apiCallRepository,
                apiResponseService,
                apiClient,
                null,   // self
                5.0 // limiter constant
            );

        // self-inject a spy
        service = Mockito.spy(rawService);
        ReflectionTestUtils.setField(service, "self", service);
    }

    private static final Instant dueDttm = Instant.now().plus(Duration.ofDays(1));
    
    private LastfmApiCall createApiCall() {
        return LastfmApiCall.builder()
                .id(1L)
                .type(LastfmApiCallType.TAG_TOP_TAGS)
                .dataSnapshotId(1L)
                .dueDttm(dueDttm)
                .status(ApiCallStatus.PENDING)
                .build();
    }

    @Test
    void triggerApiCalls_shouldProcessAllUnexpiredCalls_whenCalled() {
        // given
        LastfmApiCall apiCall = createApiCall();
        when(apiCallRepository.findAllUnprocessedUnexpired()).thenReturn(List.of(apiCall));
        doNothing().when(service).makeApiCall(any(LastfmApiCall.class));

        // when
        service.triggerApiCalls();

        // then
        verify(service).makeApiCall(apiCall);
    }

    @Test
    void makeApiCall_shouldUpdateStatusToProcessing_whenStarted() {
        // given
        LastfmApiCall apiCall = createApiCall();
        when(apiClient.makeApiCall(apiCall)).thenReturn("{}");
        when(apiResponseService.createResponse(any())).thenReturn(1L);

        // when
        service.makeApiCall(apiCall);

        // then
        verify(service, times(2)).updateApiCallStatus(eq(apiCall), any(ApiCallStatus.class));
        verify(service).updateApiCallStatus(apiCall, ApiCallStatus.PROCESSING);
        verify(service).updateApiCallStatus(apiCall, ApiCallStatus.SUCCESSFUL);
    }

    @Test
    void makeApiCall_shouldUpdateStatusToSuccessful_whenCompleted() {
        // given
        LastfmApiCall apiCall = createApiCall();
        when(apiClient.makeApiCall(apiCall)).thenReturn("{}");
        when(apiResponseService.createResponse(any())).thenReturn(1L);

        // when
        service.makeApiCall(apiCall);

        // then
        assertEquals(ApiCallStatus.SUCCESSFUL, apiCall.getStatus());
        verify(apiResponseService).createResponse(any());
    }

    @Test
    void makeApiCall_shouldUpdateStatusToFailed_whenExceptionThrown() {
        // given
        LastfmApiCall apiCall = createApiCall();
        when(apiClient.makeApiCall(apiCall)).thenThrow(new RuntimeException("API error"));

        // when
        service.makeApiCall(apiCall);

        // then
        assertEquals(ApiCallStatus.FAILED, apiCall.getStatus());
        verify(apiResponseService, never()).createResponse(any());
    }
}
