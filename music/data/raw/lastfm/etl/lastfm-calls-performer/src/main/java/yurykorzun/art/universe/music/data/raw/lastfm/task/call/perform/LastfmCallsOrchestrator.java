package yurykorzun.art.universe.music.data.raw.lastfm.task.call.perform;

import com.google.common.util.concurrent.RateLimiter;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import yurykorzun.art.universe.common.config.client.ConfigPropertyHolder;
import yurykorzun.art.universe.music.data.raw.lastfm.config.LastfmPerformerProperty;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.service.LastfmApiCallService;

import java.util.Collection;

@Service
@Slf4j
public class LastfmCallsOrchestrator {

    private final LastfmApiCallService apiCallService;
    private final LastfmApiCallExecutor executor;
    private final ConfigPropertyHolder configPropertyHolder;

    private RateLimiter rateLimiter;

    public LastfmCallsOrchestrator(
        LastfmApiCallService apiCallService,
        LastfmApiCallExecutor executor,
        ConfigPropertyHolder configPropertyHolder
    ) {
        this.apiCallService = apiCallService;
        this.executor = executor;
        this.configPropertyHolder = configPropertyHolder;
    }

    @PostConstruct
    public void init() {
        double callsPerSec = configPropertyHolder.getDecimal(LastfmPerformerProperty.CALLS_PER_SEC).doubleValue();
        rateLimiter = RateLimiter.create(callsPerSec);
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
