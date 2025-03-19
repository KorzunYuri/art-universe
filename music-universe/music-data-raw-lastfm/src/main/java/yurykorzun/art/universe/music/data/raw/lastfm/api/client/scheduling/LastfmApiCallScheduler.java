package yurykorzun.art.universe.music.data.raw.lastfm.api.client.scheduling;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.dto.LastfmApiCallCreateRequest;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.LastfmApiCallService;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.LastfmApiCallGeneratorsRegistry;

import java.util.List;

@Component
@Slf4j
public class LastfmApiCallScheduler {

    private final LastfmApiCallService lastfmApiCallService;

    public LastfmApiCallScheduler(LastfmApiCallService lastfmApiCallService) {
        this.lastfmApiCallService = lastfmApiCallService;
    }

    @Scheduled(fixedRateString = "${scheduling.lastfm.api.calls.generate.fixedRate}")
    public void generateApiCalls() {
        final String logPrefix = "Lastfm api calls generation";
        log.info("{}: Start", logPrefix);
        LastfmApiCallGeneratorsRegistry.getRegistry()
                .forEach((key, value) -> {
                    log.info("{}: Expiring  for type {}", logPrefix, key.getMethod());
                    lastfmApiCallService.expireApiCallsForType(key);
                    log.info("{}: Generating for type {}", logPrefix, key.getMethod());
                    List<LastfmApiCallCreateRequest> generated = value.generateApiCalls();
                    log.info("{}: Generated {} for type {}", logPrefix, generated.size(), key.getMethod());
                    lastfmApiCallService.createApiCalls(generated);
                });
        log.info("{}: finish", logPrefix);
    }

    @Scheduled(fixedRateString  = "${scheduling.lastfm.api.calls.fetch.fixedRate}")
    public void triggerApiCalls() {
        final String logPrefix = "Lastfm api calls performing";
        log.info("{}: start", logPrefix);
        lastfmApiCallService.triggerApiCalls();
        log.info("{}: finish", logPrefix);
    }

}
