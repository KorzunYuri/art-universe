package yurykorzun.art.universe.music.data.raw.lastfm.api.client.scheduling;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.LastfmApiCallService;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.LastfmApiCallGeneratorsRegistry;

import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class LastfmApiCallScheduler {

    private final LastfmApiCallService lastfmApiCallService;

    public LastfmApiCallScheduler(LastfmApiCallService lastfmApiCallService) {
        this.lastfmApiCallService = lastfmApiCallService;
    }

    @Scheduled(fixedDelayString = "${scheduling.lastfm.api.calls.generate.fixedDelaySecs}", timeUnit = TimeUnit.SECONDS)
    public void generateApiCalls() {
        final String logPrefix = "Lastfm api calls generation";
        log.info("{}: start", logPrefix);
        LastfmApiCallGeneratorsRegistry.getRegistry()
                .forEach((key, value) -> {
                    log.info("{} for {}: start", logPrefix, key);
                    value.createApiCalls();
                    log.info("{} for {}: finish", logPrefix, key);
                });
        log.info("{}: finish", logPrefix);
    }

    @Scheduled(fixedDelayString  = "${scheduling.lastfm.api.calls.fetch.fixedDelaySecs}", timeUnit = TimeUnit.SECONDS)
    public void triggerApiCalls() {
        final String logPrefix = "Lastfm api calls performing";
        log.info("{}: start", logPrefix);
        lastfmApiCallService.triggerApiCalls();
        log.info("{}: finish", logPrefix);
    }

}
