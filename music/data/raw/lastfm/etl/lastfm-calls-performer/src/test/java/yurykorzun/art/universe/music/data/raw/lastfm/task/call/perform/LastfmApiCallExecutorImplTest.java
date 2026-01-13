package yurykorzun.art.universe.music.data.raw.lastfm.task.call.perform;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import yurykorzun.art.universe.common.data.raw.etl.entity.ApiCallStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.service.LastfmApiCallService;
import yurykorzun.art.universe.music.data.raw.lastfm.integration.LastfmApiClient;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.service.LastfmApiResponseService;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LastfmApiCallExecutorImplTest {

    @Mock
    private LastfmApiCallService apiCallService;
    @Mock
    private LastfmApiResponseService apiResponseService;
    @Mock
    private LastfmApiClient apiClient;

    private LastfmApiCallExecutorImpl executor;

    @BeforeEach
    void setUp() {
        // first create service without self-proxy
        LastfmApiCallExecutorImpl rawService =
            new LastfmApiCallExecutorImpl(
                apiCallService,
                apiResponseService,
                apiClient,
                null,   // self
                5.0 // limiter constant
            );

        // self-inject a spy
        executor = Mockito.spy(rawService);
        ReflectionTestUtils.setField(executor, "self", executor);
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
    void executeApiCalls_shouldProcessAllUnexpiredCalls_whenCalled() {
        // given
        LastfmApiCall apiCall = createApiCall();
        when(apiCallService.findAllUnprocessedUnexpired()).thenReturn(List.of(apiCall));
        doNothing().when(executor).performApiCall(any(LastfmApiCall.class));

        // when
        executor.executeApiCalls();

        // then
        verify(executor).performApiCall(apiCall);
    }

    @Test
    void makeApiCall_shouldUpdateStatusToProcessing_whenStarted() {
        // given
        LastfmApiCall apiCall = createApiCall();
        when(apiClient.makeApiCall(apiCall)).thenReturn("{}");
        when(apiResponseService.createResponse(any())).thenReturn(1L);

        // when
        executor.performApiCall(apiCall);

        // then
        verify(apiCallService, times(2)).updateApiCallStatus(eq(apiCall), any(ApiCallStatus.class));
        verify(apiCallService).updateApiCallStatus(apiCall, ApiCallStatus.PROCESSING);
        verify(apiCallService).updateApiCallStatus(apiCall, ApiCallStatus.SUCCESSFUL);
    }

    @Test
    void makeApiCall_shouldUpdateStatusToSuccessful_whenCompleted() {
        // given
        LastfmApiCall apiCall = createApiCall();
        when(apiClient.makeApiCall(apiCall)).thenReturn("{}");
        when(apiResponseService.createResponse(any())).thenReturn(1L);

        // when
        executor.performApiCall(apiCall);

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
        executor.performApiCall(apiCall);

        // then
        assertEquals(ApiCallStatus.FAILED, apiCall.getStatus());
        verify(apiResponseService, never()).createResponse(any());
    }
}
