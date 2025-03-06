package yurykorzun.art.universe.music.data.raw.lastfm.apiclient.method.tag.toptag;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.common.data.raw.task.dto.TaskRunRequest;
import yurykorzun.art.universe.music.data.raw.lastfm.apiclient.dto.LastfmApiCallCreateRequest;
import yurykorzun.art.universe.music.data.raw.lastfm.apiclient.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.apiclient.service.LastfmApiCallService;
import yurykorzun.art.universe.music.data.raw.lastfm.task.entity.LastfmTaskType;

@Component
@Qualifier("music_lastfm_task_tags.topTags_KafkaListenerCallback")
@Slf4j
public class LastfmTagTopTagConsumer {

    private final LastfmApiCallService lastfmApiCallService;

    public LastfmTagTopTagConsumer(LastfmApiCallService lastfmApiCallService) {
        this.lastfmApiCallService = lastfmApiCallService;
    }

    public void handleMessage(TaskRunRequest message) {
        log.info("received message {}", message.toString());
        if (LastfmTaskType.TAGS_TOP_TAGS == message.type()) {
            lastfmApiCallService.createRequest(
                    LastfmApiCallCreateRequest.builder()
                            .type(LastfmApiCallType.TAG_TOP_TAGS)
                            .dueDttm(message.dueDttm())
                        .build()
            );
        } else {
            throw new IllegalArgumentException("Unknown data collection message type: " + message.type());
        }
    }
}
