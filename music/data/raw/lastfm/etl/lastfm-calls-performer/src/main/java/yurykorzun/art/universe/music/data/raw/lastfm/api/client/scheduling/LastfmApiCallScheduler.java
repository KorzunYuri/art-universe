package yurykorzun.art.universe.music.data.raw.lastfm.api.client.scheduling;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.LastfmApiCallService;
import yurykorzun.art.universe.music.data.raw.lastfm.maintenance.service.TaskCoordinator;

import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class LastfmApiCallScheduler {

    public static final String TASK_NAME_API_CALLS_EXECUTION = "api-calls-execution";
    private final LastfmApiCallService lastfmApiCallService;
    private final TaskCoordinator coordinator;

    public LastfmApiCallScheduler(LastfmApiCallService lastfmApiCallService, TaskCoordinator coordinator) {
        this.lastfmApiCallService = lastfmApiCallService;
        this.coordinator = coordinator;
    }

    @Scheduled(
        fixedDelayString  = "${lastfm.client.calls.fixedDelaySecs}",
        timeUnit = TimeUnit.SECONDS
    )
    public void triggerApiCalls() {
        coordinator.executeIfAllowed(() -> {
            log.info("start API calls performing");
            lastfmApiCallService.triggerApiCalls();
            log.info("finished API calls performing");
        }, TASK_NAME_API_CALLS_EXECUTION);
    }

}
