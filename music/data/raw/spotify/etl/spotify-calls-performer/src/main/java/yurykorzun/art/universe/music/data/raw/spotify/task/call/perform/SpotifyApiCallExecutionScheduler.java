package yurykorzun.art.universe.music.data.raw.spotify.task.call.perform;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SpotifyApiCallExecutionScheduler {

    private final SpotifyCallsOrchestrator orchestrator;

    public SpotifyApiCallExecutionScheduler(SpotifyCallsOrchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    public void executeWork() {
        try {
            log.info("start API calls performing");
            orchestrator.orchestrateApiCalls();
            log.info("finished API calls performing");
        } catch (Exception ex) {
            log.error("Error during API calls performing", ex);
        }
    }
}
