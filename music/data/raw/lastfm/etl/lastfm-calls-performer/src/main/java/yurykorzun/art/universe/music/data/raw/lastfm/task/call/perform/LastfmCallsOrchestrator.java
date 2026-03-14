package yurykorzun.art.universe.music.data.raw.lastfm.task.call.perform;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import yurykorzun.art.universe.data.raw.common.integration.AdaptiveRateLimiter;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.service.LastfmApiCallService;

import java.util.Collection;

@Service
@Slf4j
public class LastfmCallsOrchestrator {

    private final LastfmApiCallService apiCallService;
    private final LastfmApiCallExecutor executor;
    private final AdaptiveRateLimiter rateLimiter;

    public LastfmCallsOrchestrator(
        LastfmApiCallService apiCallService,
        LastfmApiCallExecutor executor,
        AdaptiveRateLimiter rateLimiter
    ) {
        this.apiCallService = apiCallService;
        this.executor = executor;
        this.rateLimiter = rateLimiter;
    }

    public void orchestrateApiCalls() {
        //   TODO design complex priority logic to fit LastFm API calls rate limit
        Collection<LastfmApiCall> apiCalls = apiCallService.findAllUnprocessedUnexpired();
        apiCalls.forEach(apiCall -> {
            log.info("initiating API call {} of type {} for entity {}: {}",
                apiCall.getId(),
                apiCall.getType(),
                apiCall.getEntityType(),
                apiCall.getEntityId()
            );
            try {
                rateLimiter.acquire();
                executor.execute(apiCall);
                rateLimiter.recordSuccess();
                log.info("API call has been performed");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Rate limiter sleep interrupted for api_call {}", apiCall.getId());
            } catch (Exception ex) {
                log.error("Failed to process API call {}: {}", apiCall.getId(), ex.getMessage(), ex);
            }
        });
    }
}
