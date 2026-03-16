package yurykorzun.art.universe.music.data.raw.spotify.application;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SpotifyStagingApplicationDispatcher {

    private final StagingApplicationScheduler stagingApplicationScheduler;

    public SpotifyStagingApplicationDispatcher(StagingApplicationScheduler stagingApplicationScheduler) {
        this.stagingApplicationScheduler = stagingApplicationScheduler;
    }

    @PostConstruct
    public void start() {
        // Applicator always uses DB polling for SEALED iterations.
        // Dispatcher pattern added for consistency with other modules.
        log.info("Starting StagingApplicationScheduler (DB-polling mode)");
        stagingApplicationScheduler.start();
    }
}
