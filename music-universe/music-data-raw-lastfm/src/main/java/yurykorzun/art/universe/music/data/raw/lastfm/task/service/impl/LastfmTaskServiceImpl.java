package yurykorzun.art.universe.music.data.raw.lastfm.task.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yurykorzun.art.universe.common.data.raw.task.entity.TaskStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.dto.LastfmApiCallCreateRequest;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.LastfmApiCallService;
import yurykorzun.art.universe.music.data.raw.lastfm.task.dto.LastfmTaskCreateRequest;
import yurykorzun.art.universe.music.data.raw.lastfm.task.entity.LastfmTask;
import yurykorzun.art.universe.music.data.raw.lastfm.task.repository.LastfmTaskRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.task.service.LastfmTaskService;

import java.time.Duration;
import java.time.Instant;

@Service
public class LastfmTaskServiceImpl implements LastfmTaskService {

    private final LastfmTaskRepository repository;
    private final LastfmApiCallService lastfmApiCallService;

    public LastfmTaskServiceImpl(LastfmTaskRepository taskRepository, LastfmApiCallService lastfmApiCallService) {
        this.repository = taskRepository;
        this.lastfmApiCallService = lastfmApiCallService;
    }


    @Override
    @Transactional
    public long createRequest(LastfmTaskCreateRequest dto) {

        LastfmTask task = repository.save(dtoToTask(dto));
        lastfmApiCallService.createApiCall(createTagTopTagsApiCallRequest());

        return task.getId();
    }

    @Override
    @Transactional
    public void setStatus(long id, TaskStatus status) throws IllegalStateException {
        LastfmTask task = repository.getReferenceById(id);
        task.setStatus(status);
        repository.save(task);
    }

    private LastfmTask dtoToTask(LastfmTaskCreateRequest dto) {
        return LastfmTask.builder()
                .type(dto.getTaskType())
                .dueDttm(Instant.now().plus(dto.getTaskType().getDueDuration()))
                .build();
    }

    private LastfmApiCallCreateRequest createTagTopTagsApiCallRequest() {
        return LastfmApiCallCreateRequest.builder()
                .type(LastfmApiCallType.TAG_TOP_TAGS)
                .dueDttm(Instant.now().plus(Duration.ofDays(1)))
            .build();
    }
}
