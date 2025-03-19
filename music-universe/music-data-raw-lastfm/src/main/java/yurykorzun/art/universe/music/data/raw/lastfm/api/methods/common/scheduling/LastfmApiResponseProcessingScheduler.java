package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.scheduling;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.LastfmApiResponseService;

@Component
@Slf4j
public class LastfmApiResponseProcessingScheduler {

    private final LastfmApiResponseService apiResponseService;

    public LastfmApiResponseProcessingScheduler(LastfmApiResponseService apiResponseService) {
        this.apiResponseService = apiResponseService;
    }

    @Scheduled(fixedRateString = "${scheduling.lastfm.api.responses.parse.fixedRate}")
    public void triggerResponsesProcessing() {
        log.info("Lastfm API responses processing: triggered");
        apiResponseService.triggerResponsesProcessing();
        log.info("Lastfm API responses processing: trigger finished");
    }

}
