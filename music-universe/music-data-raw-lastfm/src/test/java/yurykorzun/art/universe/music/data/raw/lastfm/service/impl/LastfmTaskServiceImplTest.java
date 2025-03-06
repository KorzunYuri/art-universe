package yurykorzun.art.universe.music.data.raw.lastfm.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yurykorzun.art.universe.common.persistence.entity.TaskStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.apiclient.dto.LastfmApiCallCreateRequest;
import yurykorzun.art.universe.music.data.raw.lastfm.apiclient.service.LastfmApiCallService;
import yurykorzun.art.universe.music.data.raw.lastfm.task.dto.LastfmTaskCreateRequest;
import yurykorzun.art.universe.music.data.raw.lastfm.task.entity.LastfmTask;
import yurykorzun.art.universe.music.data.raw.lastfm.task.entity.LastfmTaskType;
import yurykorzun.art.universe.music.data.raw.lastfm.task.repository.LastfmTaskRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.task.service.impl.LastfmTaskServiceImpl;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LastfmTaskServiceImplTest {

    @Mock
    private LastfmTaskRepository taskRepository;

    @Mock
    private LastfmApiCallService lastfmApiCallService;

    @InjectMocks
    private LastfmTaskServiceImpl taskService;

    @Test
    void testLastfmTaskCreation() {
        // given valid request for task creation
        LastfmTaskCreateRequest request = LastfmTaskCreateRequest.builder()
                .taskType(LastfmTaskType.TAGS_TOP_TAGS)
            .build();
        LastfmTask savedTask = LastfmTask.builder()
                .id(1L)
                .type(request.getTaskType())
                .dueDttm(Instant.now().plus(request.getTaskType().getDueDuration()))
            .build();

        when(taskRepository.save(any(LastfmTask.class))).thenReturn(savedTask);
        when(lastfmApiCallService.create(any(LastfmApiCallCreateRequest.class))).thenReturn(100L);

        // when
        long returnedTaskId = taskService.createRequest(request);

        // then
        assertEquals(1L, returnedTaskId);
        verify(taskRepository, times(1)).save(any(LastfmTask.class));
        verify(lastfmApiCallService, times(1)).create(any(LastfmApiCallCreateRequest.class));
    }

    @Test
    void testLastfmTaskStatusUpdate() {
        // given
        LastfmTask task = LastfmTask.builder()
                .id(1L)
                .type(LastfmTaskType.TAGS_TOP_TAGS)
                .dueDttm(Instant.now().plus(Duration.ofDays(1)))
            .build();
        when(taskRepository.getReferenceById(1L)).thenReturn(task);

        // when
        taskService.setStatus(1L, TaskStatus.PROCESSING);

        // then
        assertEquals(TaskStatus.PROCESSING, task.getStatus());
        verify(taskRepository, times(1)).getReferenceById(1L);
        verify(taskRepository, times(1)).save(task);
    }
}