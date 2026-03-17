package yurykorzun.art.universe.music.data.raw.lastfm.task.response.process;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.service.LastfmApiResponseService;

@Component
@Slf4j
public class LastfmApiResponseProcessingScheduler {

    public static final String TASK_NAME_API_RESPONSES_PROCESSING = "api-responses-processing";

    private final LastfmApiResponseService apiResponseService;

    public LastfmApiResponseProcessingScheduler(LastfmApiResponseService apiResponseService) {
        this.apiResponseService = apiResponseService;
    }

    public void executeWork() {
        try {
            log.info("start API responses processing");
            apiResponseService.processResponses();
            log.info("finished API responses processing");
        } catch (Exception ex) {
            log.error("Error during API responses processing", ex);
        }
    }
}
