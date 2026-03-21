package yurykorzun.art.universe.music.data.raw.spotify.task.call.perform;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import yurykorzun.art.universe.data.raw.common.etl.entity.ApiCallStatus;
import yurykorzun.art.universe.data.raw.common.integration.AdaptiveRateLimiter;
import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.SpotifyApiCall;
import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.SpotifyApiCallType;
import yurykorzun.art.universe.music.data.raw.spotify.etl.service.SpotifyApiCallService;
import yurykorzun.art.universe.music.data.raw.spotify.etl.service.SpotifyApiResponseService;
import yurykorzun.art.universe.music.data.raw.spotify.integration.SpotifyApiClient;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpotifyApiCallExecutorImplTest {

    @Mock private SpotifyApiCallService apiCallService;
    @Mock private SpotifyApiResponseService apiResponseService;
    @Mock private SpotifyApiClient apiClient;
    @Mock private AdaptiveRateLimiter rateLimiter;
    @Mock private SpotifyApiCallExecutorImpl self;

    private SpotifyApiCallExecutorImpl executor;

    @BeforeEach
    void setUp() {
        executor = new SpotifyApiCallExecutorImpl(apiCallService, apiResponseService, apiClient, rateLimiter, self);
    }

    private SpotifyApiCall buildCall() {
        return SpotifyApiCall.builder()
            .type(SpotifyApiCallType.ARTIST_GET)
            .spotifyId("artist-123")
            .dueDttm(Instant.now().plus(1, ChronoUnit.DAYS))
            .build();
    }

    @Test
    void execute_shouldReturnTrue_andFinalizeAsSuccessful_whenApiCallSucceeds() {
        SpotifyApiCall call = buildCall();
        when(self.makeApiCallWithRetry(call)).thenReturn("{\"id\":\"123\"}");

        boolean result = executor.execute(call);

        assertThat(result).isTrue();
        verify(apiCallService).updateApiCallStatus(call, ApiCallStatus.PROCESSING);
        verify(apiResponseService).createResponse(call, "{\"id\":\"123\"}");
        verify(apiCallService).finalizeApiCall(call, ApiCallStatus.SUCCESSFUL, 200, null);
        verify(rateLimiter).recordSuccess();
    }

    @Test
    void execute_shouldReturnFalse_andMarkForRetry_on429() {
        SpotifyApiCall call = buildCall();
        HttpClientErrorException tooManyRequests = HttpClientErrorException.create(
            HttpStatus.TOO_MANY_REQUESTS, "Too Many Requests", HttpHeaders.EMPTY, null, null);
        when(self.makeApiCallWithRetry(call)).thenThrow(tooManyRequests);

        boolean result = executor.execute(call);

        assertThat(result).isFalse();
        verify(apiCallService).markForRetry(call);
        verify(rateLimiter).record429();
        verify(apiCallService, never()).finalizeApiCall(any(), any(), any(), any());
    }

    @Test
    void execute_shouldReturnFalse_andFinalizeWithHttpStatus_whenHttpClientError() {
        SpotifyApiCall call = buildCall();
        HttpClientErrorException notFound = HttpClientErrorException.create(
            HttpStatus.NOT_FOUND, "Not Found", HttpHeaders.EMPTY, null, null);
        when(self.makeApiCallWithRetry(call)).thenThrow(notFound);

        boolean result = executor.execute(call);

        assertThat(result).isFalse();
        verify(apiCallService).finalizeApiCall(eq(call), eq(ApiCallStatus.FAILED), eq(404), anyString());
        verify(rateLimiter, never()).record429();
        verify(rateLimiter, never()).recordSuccess();
    }

    @Test
    void execute_shouldReturnFalse_andFinalizeWithNullStatus_whenGenericException() {
        SpotifyApiCall call = buildCall();
        when(self.makeApiCallWithRetry(call)).thenThrow(new RuntimeException("network error"));

        boolean result = executor.execute(call);

        assertThat(result).isFalse();
        verify(apiCallService).finalizeApiCall(call, ApiCallStatus.FAILED, null, "network error");
        verify(rateLimiter, never()).recordSuccess();
    }

    @Test
    void makeApiCallWithRetry_shouldDelegateToApiClient() {
        SpotifyApiCall call = buildCall();
        when(apiClient.makeApiCall(call)).thenReturn("{\"response\":true}");

        String result = executor.makeApiCallWithRetry(call);

        assertThat(result).isEqualTo("{\"response\":true}");
        verify(apiClient).makeApiCall(call);
    }
}
