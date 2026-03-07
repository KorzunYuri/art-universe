package yurykorzun.art.universe.music.data.raw.spotify.task.call.perform;

import com.google.common.util.concurrent.RateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.SpotifyApiCall;
import yurykorzun.art.universe.music.data.raw.spotify.etl.service.SpotifyApiCallService;

import java.util.List;

@Service
@Slf4j
public class SpotifyCallsOrchestrator {

    private final SpotifyApiCallService apiCallService;
    private final SpotifyApiCallExecutor executor;
    private final RateLimiter rateLimiter;

    public SpotifyCallsOrchestrator(
        SpotifyApiCallService apiCallService,
        SpotifyApiCallExecutor executor,
        @Value("${spotify.tasks.calls-perform.calls-per-sec}") double callsPerSec
    ) {
        this.apiCallService = apiCallService;
        this.executor = executor;
        this.rateLimiter = RateLimiter.create(callsPerSec);
    }

    public void orchestrateApiCalls() {
        List<SpotifyApiCall> apiCalls = apiCallService.findAllCreatedUnexpired();
        apiCalls.forEach(apiCall -> {
            log.info("initiating API call {} of type {} for spotifyId {}",
                apiCall.getId(), apiCall.getType().getMethod(), apiCall.getSpotifyId());
            rateLimiter.acquire();
            try {
                executor.execute(apiCall);
                log.info("API call {} completed", apiCall.getId());
            } catch (Exception e) {
                log.error("Unhandled error processing api_call {}: {}", apiCall.getId(), e.getMessage(), e);
            }
        });
    }
}
