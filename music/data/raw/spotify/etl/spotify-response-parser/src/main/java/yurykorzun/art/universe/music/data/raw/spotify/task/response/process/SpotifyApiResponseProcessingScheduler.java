package yurykorzun.art.universe.music.data.raw.spotify.task.response.process;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class SpotifyApiResponseProcessingScheduler {

    private final SpotifyApiResponseProcessingOrchestrator orchestrator;

    public SpotifyApiResponseProcessingScheduler(SpotifyApiResponseProcessingOrchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    @Scheduled(
        fixedDelayString = "${spotify.scheduling.response-parse.fixed-delay-secs}",
        timeUnit = TimeUnit.SECONDS
    )
    public void triggerResponsesProcessing() {
        log.info("start API responses processing");
        orchestrator.processResponses();
        log.info("finished API responses processing");
    }
}
