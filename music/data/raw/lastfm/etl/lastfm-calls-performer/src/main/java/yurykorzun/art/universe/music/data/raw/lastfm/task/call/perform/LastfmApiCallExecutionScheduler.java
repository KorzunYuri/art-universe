package yurykorzun.art.universe.music.data.raw.lastfm.task.call.perform;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class LastfmApiCallExecutionScheduler {

    private final LastfmApiCallExecutor lastfmApiCallExecutor;

    public LastfmApiCallExecutionScheduler(LastfmApiCallExecutor lastfmApiCallExecutor) {
        this.lastfmApiCallExecutor = lastfmApiCallExecutor;
    }

    @Scheduled(
        fixedDelayString  = "${lastfm.scheduling.calls-perform.fixed-delay-secs}",
        timeUnit = TimeUnit.SECONDS
    )
    public void triggerApiCallsExecution() {
        log.info("start API calls performing");
        lastfmApiCallExecutor.executeApiCalls();
        log.info("finished API calls performing");
    }

}
