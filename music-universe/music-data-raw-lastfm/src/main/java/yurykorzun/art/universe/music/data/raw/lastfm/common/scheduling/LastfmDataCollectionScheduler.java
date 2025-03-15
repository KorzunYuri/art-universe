package yurykorzun.art.universe.music.data.raw.lastfm.common.scheduling;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.music.data.raw.lastfm.task.dto.LastfmTaskCreateRequest;
import yurykorzun.art.universe.music.data.raw.lastfm.task.entity.LastfmTaskType;
import yurykorzun.art.universe.music.data.raw.lastfm.task.service.LastfmTaskService;

@Component
public class LastfmDataCollectionScheduler {

    private final LastfmTaskService tagsRequestService;

    public LastfmDataCollectionScheduler(LastfmTaskService tagsRequestService) {
        this.tagsRequestService = tagsRequestService;
    }

    @Scheduled(cron = "${scheduling.lastfm.tasks.tag.topTags.cron}")
    public void generateTagsRequest() {
        tagsRequestService.createRequest(
                LastfmTaskCreateRequest.builder()
                        .taskType(LastfmTaskType.TAGS_TOP_TAGS)
                    .build());
    }
}
