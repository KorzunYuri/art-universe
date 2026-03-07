package yurykorzun.art.universe.music.data.raw.spotify.application;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class StagingApplicationScheduler {

    private final StagingApplicationService applicationService;

    public StagingApplicationScheduler(StagingApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @Scheduled(
        fixedDelayString = "${spotify.scheduling.staging-apply.fixed-delay-secs}",
        timeUnit = TimeUnit.SECONDS
    )
    public void triggerStagingApplication() {
        log.info("start staging application");
        applicationService.applySealedIterations();
        log.info("finished staging application");
    }
}
