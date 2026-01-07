package yurykorzun.art.universe.music.data.raw.lastfm.api.client.scheduling;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.LastfmApiCallGeneratorsRegistry;
import yurykorzun.art.universe.music.data.raw.lastfm.maintenance.service.TaskCoordinator;

import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class LastfmApiCallGenerationScheduler {

    public static final String TASK_NAME_API_CALLS_GENERATION = "api-calls-generation";
    private final TaskCoordinator coordinator;

    public LastfmApiCallGenerationScheduler(TaskCoordinator coordinator) {
        this.coordinator = coordinator;
    }

    @Scheduled(
        fixedDelayString = "${lastfm.client.calls.generate.fixedDelaySecs}",
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
}
