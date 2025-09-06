package yurykorzun.art.universe.music.data.raw.lastfm.api.client.scheduling;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.LastfmApiCallGeneratorsRegistry;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.LastfmApiCallService;
import yurykorzun.art.universe.music.data.raw.lastfm.maintenance.service.TaskCoordinator;

import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class LastfmApiCallScheduler {

    public static final String TASK_NAME_API_CALLS_GENERATION = "api-calls-generation";
    public static final String TASK_NAME_API_CALLS_EXECUTION = "api-calls-execution";
    private final LastfmApiCallService lastfmApiCallService;
    private final TaskCoordinator coordinator;

    public LastfmApiCallScheduler(LastfmApiCallService lastfmApiCallService, TaskCoordinator coordinator) {
        this.lastfmApiCallService = lastfmApiCallService;
        this.coordinator = coordinator;
    }

    @Scheduled(
        fixedDelayString = "${scheduling.lastfm.api.calls.generate.fixedDelaySecs}",
        timeUnit = TimeUnit.SECONDS
    )
    public void generateApiCalls() {
        coordinator.executeIfAllowed(() -> {
            log.info("start API calls generation");
            LastfmApiCallGeneratorsRegistry.getRegistry()
                    .forEach((apiCallType, generator) -> {
                        log.info("start API calls generation for method {}", generator.getApiCallType().getMethod());
                        generator.createApiCalls();
                        log.info("finished API calls generation for method {}", generator.getApiCallType().getMethod());
                    });
            log.info("finished API calls generation");
        }, TASK_NAME_API_CALLS_GENERATION);
    }

    @Scheduled(
        fixedDelayString  = "${scheduling.lastfm.api.calls.fetch.fixedDelaySecs}",
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
