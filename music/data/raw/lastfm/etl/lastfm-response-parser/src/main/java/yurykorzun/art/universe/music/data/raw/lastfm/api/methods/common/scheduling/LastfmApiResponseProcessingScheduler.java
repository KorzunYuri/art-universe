package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.scheduling;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.LastfmApiResponseService;

import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class LastfmApiResponseProcessingScheduler {

    public static final String TASK_NAME_API_RESPONSES_PROCESSING = "api-responses-processing";
    private final LastfmApiResponseService apiResponseService;

    public LastfmApiResponseProcessingScheduler(LastfmApiResponseService apiResponseService) {
        this.apiResponseService = apiResponseService;
    }

    @Scheduled(
        fixedDelayString = "${lastfm.scheduling.response-parse.fixed-delay-secs}",
        timeUnit = TimeUnit.SECONDS
    )
    public void triggerResponsesProcessing() {
        log.info("start API responses processing");
        apiResponseService.processResponses();
        log.info("finished API responses processing");
    }

}
