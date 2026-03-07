package yurykorzun.art.universe.music.data.raw.spotify.task.call.perform;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class SpotifyApiCallExecutionScheduler {

    private final SpotifyCallsOrchestrator orchestrator;

    public SpotifyApiCallExecutionScheduler(SpotifyCallsOrchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    @Scheduled(
        fixedDelayString = "${spotify.scheduling.calls-perform.fixed-delay-secs}",
        timeUnit = TimeUnit.SECONDS
    )
    public void triggerApiCallsExecution() {
        log.info("start API calls performing");
        orchestrator.orchestrateApiCalls();
        log.info("finished API calls performing");
    }
}
