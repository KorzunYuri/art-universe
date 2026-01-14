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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
                null   // self
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
    void execute_shouldUpdateStatusToProcessing_whenStarted() {
        // given
        LastfmApiCall apiCall = createApiCall();
        when(apiClient.makeApiCall(apiCall)).thenReturn("{}");
        when(apiResponseService.createResponse(any())).thenReturn(1L);

        // when
        executor.execute(apiCall);

        // then
        verify(apiCallService).updateApiCallStatus(apiCall, ApiCallStatus.PROCESSING);
    }

    @Test
    void execute_shouldUpdateStatusToSuccessful_whenCompleted() {
        // given
        LastfmApiCall apiCall = createApiCall();
        when(apiClient.makeApiCall(apiCall)).thenReturn("{}");
        when(apiResponseService.createResponse(any())).thenReturn(1L);

        // when
        executor.execute(apiCall);

        // then
        verify(apiCallService).updateApiCallStatus(apiCall, ApiCallStatus.SUCCESSFUL);
        verify(apiResponseService).createResponse(any());
    }

    @Test
    void execute_shouldUpdateStatusToFailed_whenExceptionThrown() {
        // given
        LastfmApiCall apiCall = createApiCall();
        when(apiClient.makeApiCall(apiCall)).thenThrow(new RuntimeException("API error"));

        // when
        executor.execute(apiCall);

        // then
        verify(apiCallService).updateApiCallStatus(apiCall, ApiCallStatus.PROCESSING);
        verify(apiCallService).updateApiCallStatus(apiCall, ApiCallStatus.FAILED);
        verify(apiResponseService, never()).createResponse(any());
    }

    @Test
    void execute_shouldCallApiClientAndResponseService_whenSuccessful() {
        // given
        LastfmApiCall apiCall = createApiCall();
        String responseBody = "{\"result\": \"success\"}";
        when(apiClient.makeApiCall(apiCall)).thenReturn(responseBody);
        when(apiResponseService.createResponse(any())).thenReturn(1L);

        // when
        executor.execute(apiCall);

        // then
        verify(apiClient).makeApiCall(apiCall);
        verify(apiResponseService).createResponse(any());
    }

    @Test
    void execute_shouldUpdateStatusExactlyTwice_whenSuccessful() {
        // given
        LastfmApiCall apiCall = createApiCall();
        when(apiClient.makeApiCall(apiCall)).thenReturn("{}");
        when(apiResponseService.createResponse(any())).thenReturn(1L);

        // when
        executor.execute(apiCall);

        // then
        verify(apiCallService, times(2)).updateApiCallStatus(eq(apiCall), any(ApiCallStatus.class));
    }

    @Test
    void execute_shouldUpdateStatusExactlyTwice_whenFailed() {
        // given
        LastfmApiCall apiCall = createApiCall();
        when(apiClient.makeApiCall(apiCall)).thenThrow(new RuntimeException("API error"));

        // when
        executor.execute(apiCall);

        // then
        verify(apiCallService, times(2)).updateApiCallStatus(eq(apiCall), any(ApiCallStatus.class));
    }
}
