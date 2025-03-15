package yurykorzun.art.universe.music.data.raw.lastfm.api.client.scheduling;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.LastfmApiCallService;

@Component
public class LastfmApiCallScheduler {

    private final LastfmApiCallService lastfmApiCallService;

    public LastfmApiCallScheduler(LastfmApiCallService lastfmApiCallService) {
        this.lastfmApiCallService = lastfmApiCallService;
    }

    @Scheduled(
            //cron = "${scheduling.lastfm.apicalls.fetch.cron}"
            fixedRateString  = "${scheduling.lastfm.apicalls.fetch.fixedRate}"
    )
    public void triggerApiCalls() {
        lastfmApiCallService.triggerApiCalls();
    }

}
