package yurykorzun.art.universe.music.data.raw.spotify.task.call.perform;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import yurykorzun.art.universe.data.raw.common.integration.AdaptiveRateLimiter;
import yurykorzun.art.universe.music.data.raw.spotify.common.SpotifyConstants;
import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.SpotifyApiCall;
import yurykorzun.art.universe.music.data.raw.spotify.etl.service.SpotifyApiCallService;

import java.util.List;

@Service
@Slf4j
public class SpotifyCallsOrchestrator {

    private final SpotifyApiCallService apiCallService;
    private final SpotifyApiCallExecutor executor;
    private final AdaptiveRateLimiter rateLimiter;
    private final JdbcTemplate jdbcTemplate;

    public SpotifyCallsOrchestrator(
        SpotifyApiCallService apiCallService,
        SpotifyApiCallExecutor executor,
        AdaptiveRateLimiter rateLimiter,
        JdbcTemplate jdbcTemplate
    ) {
        this.apiCallService = apiCallService;
        this.executor = executor;
        this.rateLimiter = rateLimiter;
        this.jdbcTemplate = jdbcTemplate;
    }

    public void orchestrateApiCalls() {
        List<SpotifyApiCall> apiCalls = apiCallService.findAllCreatedUnexpired();
        int successCount = 0;
        for (SpotifyApiCall apiCall : apiCalls) {
            log.info("initiating API call {} of type {} for spotifyId {}",
                apiCall.getId(), apiCall.getType().getMethod(), apiCall.getSpotifyId());
            try {
                rateLimiter.acquire();
                if (executor.execute(apiCall)) {
                    successCount++;
                }
                log.info("API call {} completed", apiCall.getId());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Rate limiter sleep interrupted for api_call {}", apiCall.getId());
                break;
            } catch (Exception e) {
                log.error("Unhandled error processing api_call {}: {}", apiCall.getId(), e.getMessage(), e);
            }
        }
        if (successCount > 0) {
            try {
                jdbcTemplate.execute("NOTIFY " + SpotifyConstants.NOTIFY_RESPONSES_READY);
                log.debug("Sent NOTIFY on channel '{}' after {} successful calls", SpotifyConstants.NOTIFY_RESPONSES_READY, successCount);
            } catch (Exception e) {
                log.warn("Failed to send NOTIFY on channel '{}': {}", SpotifyConstants.NOTIFY_RESPONSES_READY, e.getMessage());
            }
        }
    }
}
