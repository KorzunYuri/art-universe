package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.scheduling;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.LastfmApiResponseService;
import yurykorzun.art.universe.music.data.raw.lastfm.maintenance.service.TaskCoordinator;

import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class LastfmApiResponseProcessingScheduler {

    public static final String TASK_NAME_API_RESPONSES_PROCESSING = "api-responses-processing";
    private final LastfmApiResponseService apiResponseService;
    private final TaskCoordinator coordinator;

    public LastfmApiResponseProcessingScheduler(LastfmApiResponseService apiResponseService, TaskCoordinator coordinator) {
        this.apiResponseService = apiResponseService;
        this.coordinator = coordinator;
    }

    @Scheduled(
        fixedDelayString = "${lastfm.scheduling.parsing.fixedDelaySecs}",
        timeUnit = TimeUnit.SECONDS
    )
    public void triggerResponsesProcessing() {
        coordinator.executeIfAllowed(() -> {
            log.info("start API responses processing");
            apiResponseService.processResponses();
            log.info("finished API responses processing");
        }, TASK_NAME_API_RESPONSES_PROCESSING);
    }

}
