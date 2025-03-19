package yurykorzun.art.universe.music.data.raw.lastfm.api.client.scheduling;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.LastfmApiCallService;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.LastfmApiCallGeneratorsRegistry;

@Component
@Slf4j
public class LastfmApiCallScheduler {

    private final LastfmApiCallService lastfmApiCallService;

    public LastfmApiCallScheduler(LastfmApiCallService lastfmApiCallService) {
        this.lastfmApiCallService = lastfmApiCallService;
    }

    @Scheduled(fixedRateString = "${scheduling.lastfm.api.calls.generate.fixedRate}")
    public void generateApiCalls() {
        LastfmApiCallGeneratorsRegistry.getRegistry()
                .forEach((key, value) -> {
                    log.info("Expiring lastfm api calls for type {}", key.getMethod());
                    lastfmApiCallService.expireApiCallsForType(key);
                    log.info("Generating lastfm api calls for type {}", key.getMethod());
                    lastfmApiCallService.createApiCalls(value.generateApiCalls());
                });
    }

    @Scheduled(fixedRateString  = "${scheduling.lastfm.api.calls.fetch.fixedRate}")
    public void triggerApiCalls() {
        log.info("Triggered: api calls");
        lastfmApiCallService.triggerApiCalls();
    }

}
