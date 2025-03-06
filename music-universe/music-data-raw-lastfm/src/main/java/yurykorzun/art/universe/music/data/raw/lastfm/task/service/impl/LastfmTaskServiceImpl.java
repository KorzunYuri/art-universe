package yurykorzun.art.universe.music.data.raw.lastfm.task.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yurykorzun.art.universe.common.data.raw.task.messaging.TaskMessageProducer;
import yurykorzun.art.universe.common.data.raw.task.dto.TaskRunRequest;
import yurykorzun.art.universe.common.persistence.entity.TaskStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.task.dto.LastfmTaskCreateRequest;
import yurykorzun.art.universe.music.data.raw.lastfm.task.entity.LastfmTask;
import yurykorzun.art.universe.music.data.raw.lastfm.task.repository.LastfmTaskRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.task.service.LastfmTaskService;

import java.time.Instant;

@Service
public class LastfmTaskServiceImpl implements LastfmTaskService {

    private final LastfmTaskRepository repository;
    private final TaskMessageProducer messageProducer;

    public LastfmTaskServiceImpl(LastfmTaskRepository taskRepository, TaskMessageProducer messageProducer) {
        this.repository = taskRepository;
        this.messageProducer = messageProducer;
    }


    @Override
    @Transactional
    public long createRequest(LastfmTaskCreateRequest dto) {

        LastfmTask task = repository.save(dtoToTask(dto));

        messageProducer.send(buildRunRequest(task));

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

    private TaskRunRequest buildRunRequest(LastfmTask task) {
        return new TaskRunRequest(task.getId(), task.getType(), task.getDueDttm());
    }
}
