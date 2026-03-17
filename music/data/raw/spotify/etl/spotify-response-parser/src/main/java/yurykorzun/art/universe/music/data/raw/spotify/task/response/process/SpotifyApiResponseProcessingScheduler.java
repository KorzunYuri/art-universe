package yurykorzun.art.universe.music.data.raw.spotify.task.response.process;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SpotifyApiResponseProcessingScheduler {

    private final SpotifyApiResponseProcessingOrchestrator orchestrator;

    public SpotifyApiResponseProcessingScheduler(SpotifyApiResponseProcessingOrchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    public void executeWork() {
        try {
            log.info("start API responses processing");
            orchestrator.processResponses();
            log.info("finished API responses processing");
        } catch (Exception ex) {
            log.error("Error during API responses processing", ex);
        }
    }
}
