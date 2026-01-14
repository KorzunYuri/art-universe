package yurykorzun.art.universe.music.data.raw.lastfm.task.call.perform;

import com.google.common.util.concurrent.RateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.service.LastfmApiCallService;

import java.util.Collection;

@Service
@Slf4j
public class LastfmCallsOrchestrator {

    private final LastfmApiCallService apiCallService;
    private final LastfmApiCallExecutor executor;
    private final RateLimiter rateLimiter;

    public LastfmCallsOrchestrator(
        LastfmApiCallService apiCallService,
        LastfmApiCallExecutor executor,
        @Value("${lastfm.tasks.calls-perform.calls-per-sec}") double apiClientCallsPerSec
    ) {
        this.apiCallService = apiCallService;
        this.executor = executor;
        this.rateLimiter = RateLimiter.create(apiClientCallsPerSec);
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
            rateLimiter.acquire();
            try {
                executor.execute(apiCall);
                log.info("API call has been performed");
            } catch (Exception ex) {
                log.error("Failed to process API call {}: {}", apiCall.getId(), ex.getMessage(), ex);
            }
        });
    }
}
