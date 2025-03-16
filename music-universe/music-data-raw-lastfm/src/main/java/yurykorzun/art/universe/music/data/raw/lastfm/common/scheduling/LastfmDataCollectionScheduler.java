package yurykorzun.art.universe.music.data.raw.lastfm.common.scheduling;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.music.data.raw.lastfm.task.dto.LastfmTaskCreateRequest;
import yurykorzun.art.universe.music.data.raw.lastfm.task.entity.LastfmTaskType;
import yurykorzun.art.universe.music.data.raw.lastfm.task.service.LastfmTaskService;

@Component
@Slf4j
public class LastfmDataCollectionScheduler {

    private final LastfmTaskService tagsRequestService;

    public LastfmDataCollectionScheduler(LastfmTaskService tagsRequestService) {
        this.tagsRequestService = tagsRequestService;
    }

    @Scheduled(cron = "${scheduling.lastfm.tasks.tag.topTags.cron}")
    public void generateTagsRequest() {
        log.info("Triggered: tag.topTags api calls initiation");
        tagsRequestService.createRequest(
                LastfmTaskCreateRequest.builder()
                        .taskType(LastfmTaskType.TAGS_TOP_TAGS)
                    .build());
    }
}
