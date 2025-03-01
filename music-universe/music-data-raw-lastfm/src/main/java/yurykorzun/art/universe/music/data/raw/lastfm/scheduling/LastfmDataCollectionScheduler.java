package yurykorzun.art.universe.music.data.raw.lastfm.scheduling;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.common.dto.DataCollectionTaskCreateRequest;
import yurykorzun.art.universe.music.data.raw.lastfm.entity.LastfmTaskType;
import yurykorzun.art.universe.music.data.raw.lastfm.service.LastfmTaskService;

import java.time.Duration;
import java.time.Instant;

@Component
public class LastfmDataCollectionScheduler {

    private final LastfmTaskService tagsRequestService;

    public LastfmDataCollectionScheduler(LastfmTaskService tagsRequestService) {
        this.tagsRequestService = tagsRequestService;
    }

    @Scheduled(cron = "${scheduling.cron.lastfm.tags}")
    public void generateTagsRequest() {
        tagsRequestService.createRequest(
                new DataCollectionTaskCreateRequest(
                        LastfmTaskType.TAGS_TOP_TAGS,
                        Instant.now().plus(Duration.ofDays(1))
        ));
    }
}
