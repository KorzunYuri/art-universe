package yurykorzun.art.universe.music.data.raw.spotify.task.call.perform;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.SpotifyApiCall;
import yurykorzun.art.universe.music.data.raw.spotify.etl.service.SpotifyApiCallService;

import java.util.List;

@Service
@Slf4j
public class SpotifyCallsOrchestrator {

    private final SpotifyApiCallService apiCallService;
    private final SpotifyApiCallExecutor executor;
    private final AdaptiveRateLimiter rateLimiter;

    public SpotifyCallsOrchestrator(
        SpotifyApiCallService apiCallService,
        SpotifyApiCallExecutor executor,
        AdaptiveRateLimiter rateLimiter
    ) {
        this.apiCallService = apiCallService;
        this.executor = executor;
        this.rateLimiter = rateLimiter;
    }

    public void orchestrateApiCalls() {
        List<SpotifyApiCall> apiCalls = apiCallService.findAllCreatedUnexpired();
        apiCalls.forEach(apiCall -> {
            log.info("initiating API call {} of type {} for spotifyId {}",
                apiCall.getId(), apiCall.getType().getMethod(), apiCall.getSpotifyId());
            try {
                rateLimiter.acquire();
                executor.execute(apiCall);
                log.info("API call {} completed", apiCall.getId());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Rate limiter sleep interrupted for api_call {}", apiCall.getId());
            } catch (Exception e) {
                log.error("Unhandled error processing api_call {}: {}", apiCall.getId(), e.getMessage(), e);
            }
        });
    }
}
