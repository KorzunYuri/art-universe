package yurykorzun.art.universe.music.data.raw.lastfm.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yurykorzun.art.universe.common.dto.DataCollectionTaskCreateRequest;
import yurykorzun.art.universe.common.dto.DataCollectionTaskCreateResponse;
import yurykorzun.art.universe.common.messaging.MessageProducer;
import yurykorzun.art.universe.common.messaging.dto.DataCollectionTaskMessage;
import yurykorzun.art.universe.common.persistence.entity.TaskStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.entity.LastfmTask;
import yurykorzun.art.universe.music.data.raw.lastfm.entity.LastfmTaskType;
import yurykorzun.art.universe.music.data.raw.lastfm.repository.LastfmTaskRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.service.LastfmTaskService;
import yurykorzun.art.universe.music.data.raw.lastfm.utils.TimeUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class LastfmTaskServiceImpl implements LastfmTaskService {

    private final LastfmTaskRepository repository;
    private final MessageProducer messageProducer;

    public LastfmTaskServiceImpl(LastfmTaskRepository taskRepository, MessageProducer messageProducer) {
        this.repository = taskRepository;
        this.messageProducer = messageProducer;
    }

    @Override
    @Transactional
    public DataCollectionTaskCreateResponse createRequest(DataCollectionTaskCreateRequest dto) {

        //  TODO consider setting toDttm to the end of the day for timestamp operations
        Instant today = TimeUtils.truncToDays(Instant.now());
        LastfmTask task = LastfmTask.builder()
                .type(LastfmTaskType.TAGS_TOP_TAGS)
                .dueDttm(today.plus(1, ChronoUnit.DAYS))
                .build();
        task = repository.save(task);

        messageProducer.send(taskToMessage(task));

        return new DataCollectionTaskCreateResponse(task.getId());
    }

    @Override
    @Transactional
    public void setStatus(long id, TaskStatus status) throws IllegalStateException {
        LastfmTask task = repository.getReferenceById(id);
        task.setStatus(status);
        repository.save(task);
    }

    private DataCollectionTaskMessage taskToMessage(LastfmTask task) {
        return new DataCollectionTaskMessage(task.getId(), task.getType(), task.getDueDttm());
    }
}
