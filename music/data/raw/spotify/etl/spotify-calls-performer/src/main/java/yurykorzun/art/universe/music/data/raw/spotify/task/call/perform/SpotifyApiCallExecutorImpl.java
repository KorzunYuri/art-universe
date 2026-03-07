package yurykorzun.art.universe.music.data.raw.spotify.task.call.perform;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import yurykorzun.art.universe.data.raw.common.etl.entity.ApiCallStatus;
import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.SpotifyApiCall;
import yurykorzun.art.universe.music.data.raw.spotify.etl.service.SpotifyApiCallService;
import yurykorzun.art.universe.music.data.raw.spotify.etl.service.SpotifyApiResponseService;
import yurykorzun.art.universe.music.data.raw.spotify.integration.SpotifyApiClient;

@Service
@Slf4j
public class SpotifyApiCallExecutorImpl implements SpotifyApiCallExecutor {

    private final SpotifyApiCallService apiCallService;
    private final SpotifyApiResponseService apiResponseService;
    private final SpotifyApiClient apiClient;
    private final SpotifyApiCallExecutorImpl self;

    public SpotifyApiCallExecutorImpl(
        SpotifyApiCallService apiCallService,
        SpotifyApiResponseService apiResponseService,
        SpotifyApiClient apiClient,
        @Lazy SpotifyApiCallExecutorImpl self
    ) {
        this.apiCallService = apiCallService;
        this.apiResponseService = apiResponseService;
        this.apiClient = apiClient;
        this.self = self;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void execute(SpotifyApiCall call) {
        apiCallService.updateApiCallStatus(call, ApiCallStatus.PROCESSING);

        try {
            String response = self.makeApiCallWithRetry(call);
            apiResponseService.createResponse(call, response);
            apiCallService.finalizeApiCall(call, ApiCallStatus.SUCCESSFUL, 200, null);
        } catch (HttpClientErrorException e) {
            int status = e.getStatusCode().value();
            log.warn("Spotify HTTP {} for api_call {} (spotifyId={})",
                status, call.getId(), call.getSpotifyId());
            apiCallService.finalizeApiCall(call, ApiCallStatus.FAILED, status, e.getMessage());
        } catch (Exception e) {
            log.error("Failed to execute api_call {} (type={}): {}",
                call.getId(), call.getType().getMethod(), e.getMessage(), e);
            apiCallService.finalizeApiCall(call, ApiCallStatus.FAILED, null, e.getMessage());
        }
    }

    @Retryable(
        retryFor = {Exception.class},
        noRetryFor = {HttpClientErrorException.NotFound.class},
        maxAttemptsExpression = "${spotify.tasks.calls-perform.retry.max-attempts}",
        backoff = @Backoff(
            delayExpression = "${spotify.tasks.calls-perform.retry.initial-delay-ms}",
            multiplierExpression = "${spotify.tasks.calls-perform.retry.multiplier}"
        )
    )
    public String makeApiCallWithRetry(SpotifyApiCall call) {
        return apiClient.makeApiCall(call);
    }
}
