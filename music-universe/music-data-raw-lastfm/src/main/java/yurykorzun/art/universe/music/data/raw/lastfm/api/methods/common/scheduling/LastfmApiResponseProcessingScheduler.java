package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.scheduling;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.LastfmApiResponseService;

import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class LastfmApiResponseProcessingScheduler {

    private final LastfmApiResponseService apiResponseService;

    public LastfmApiResponseProcessingScheduler(LastfmApiResponseService apiResponseService) {
        this.apiResponseService = apiResponseService;
    }

    @Scheduled(fixedDelayString = "${scheduling.lastfm.api.responses.parse.fixedDelaySecs}", timeUnit = TimeUnit.SECONDS)
    public void triggerResponsesProcessing() {
        log.info("start API responses processing");
        apiResponseService.triggerResponsesProcessing();
        log.info("finished API responses processing");
    }

}
