package yurykorzun.art.universe.music.data.raw.lastfm.task.call.perform;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import yurykorzun.art.universe.data.raw.common.integration.AdaptiveRateLimiter;
import yurykorzun.art.universe.music.data.raw.lastfm.common.LastfmConstants;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.service.LastfmApiCallService;

import java.util.Collection;

@Service
@Slf4j
public class LastfmApiCallsOrchestrator {

    private final LastfmApiCallService apiCallService;
    private final LastfmApiCallExecutor executor;
    private final AdaptiveRateLimiter rateLimiter;
    private final JdbcTemplate jdbcTemplate;

    public LastfmApiCallsOrchestrator(
        LastfmApiCallService apiCallService,
        LastfmApiCallExecutor executor,
        AdaptiveRateLimiter rateLimiter,
        JdbcTemplate jdbcTemplate
    ) {
        this.apiCallService = apiCallService;
        this.executor = executor;
        this.rateLimiter = rateLimiter;
        this.jdbcTemplate = jdbcTemplate;
    }

    public void orchestrateApiCalls() {
        //   TODO design complex priority logic to fit LastFm API calls rate limit
        Collection<LastfmApiCall> apiCalls = apiCallService.findAllUnprocessedUnexpired();
        int successCount = 0;
        for (LastfmApiCall apiCall : apiCalls) {
            log.info("initiating API call {} of type {} for entity {}: {}",
                apiCall.getId(),
                apiCall.getType(),
                apiCall.getEntityType(),
                apiCall.getEntityId()
            );
            try {
                rateLimiter.acquire();
                if (executor.execute(apiCall)) {
                    successCount++;
                }
                rateLimiter.recordSuccess();
                log.info("API call has been performed");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Rate limiter sleep interrupted for api_call {}", apiCall.getId());
                break;
            } catch (Exception ex) {
                log.error("Failed to process API call {}: {}", apiCall.getId(), ex.getMessage(), ex);
            }
        }
        if (successCount > 0) {
            try {
                jdbcTemplate.execute("NOTIFY " + LastfmConstants.NOTIFY_RESPONSES_READY);
                log.debug("Sent NOTIFY on channel '{}' after {} successful calls", LastfmConstants.NOTIFY_RESPONSES_READY, successCount);
            } catch (Exception e) {
                log.warn("Failed to send NOTIFY on channel '{}': {}", LastfmConstants.NOTIFY_RESPONSES_READY, e.getMessage());
            }
        }
    }
}
