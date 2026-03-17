package yurykorzun.art.universe.music.data.raw.spotify.application;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class StagingApplicationScheduler {

    private final StagingApplicationService applicationService;
    private final SearchReconciliationService searchReconciliationService;

    public StagingApplicationScheduler(
        StagingApplicationService applicationService,
        SearchReconciliationService searchReconciliationService
    ) {
        this.applicationService = applicationService;
        this.searchReconciliationService = searchReconciliationService;
    }

    public void executeWork() {
        try {
            log.info("start staging application");
            applicationService.applySealedIterations();
            searchReconciliationService.reconcileMatchedAttempts();
            log.info("finished staging application");
        } catch (Exception ex) {
            log.error("Error during staging application", ex);
        }
    }
}
