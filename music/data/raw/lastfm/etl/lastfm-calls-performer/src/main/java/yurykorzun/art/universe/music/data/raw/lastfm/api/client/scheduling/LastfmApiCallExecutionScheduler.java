package yurykorzun.art.universe.music.data.raw.lastfm.api.client.scheduling;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.LastfmApiCallService;

import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class LastfmApiCallExecutionScheduler {

    private final LastfmApiCallService lastfmApiCallService;

    public LastfmApiCallExecutionScheduler(LastfmApiCallService lastfmApiCallService) {
        this.lastfmApiCallService = lastfmApiCallService;
    }

    @Scheduled(
        fixedDelayString  = "${lastfm.scheduling.calls-perform.fixed-delay-secs}",
        timeUnit = TimeUnit.SECONDS
    )
    public void triggerApiCallsExecution() {
        log.info("start API calls performing");
        lastfmApiCallService.executeApiCalls();
        log.info("finished API calls performing");
    }

}
