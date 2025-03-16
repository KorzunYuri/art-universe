package yurykorzun.art.universe.music.data.raw.lastfm.api.client.scheduling;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.LastfmApiCallService;

@Component
@Slf4j
public class LastfmApiCallScheduler {

    private final LastfmApiCallService lastfmApiCallService;

    public LastfmApiCallScheduler(LastfmApiCallService lastfmApiCallService) {
        this.lastfmApiCallService = lastfmApiCallService;
    }

    @Scheduled(
            //cron = "${scheduling.lastfm.apicalls.fetch.cron}"
            fixedRateString  = "${scheduling.lastfm.api.calls.fetch.fixedRate}"
    )
    public void triggerApiCalls() {
        log.info("Triggered: api calls");
        lastfmApiCallService.triggerApiCalls();
    }

}
