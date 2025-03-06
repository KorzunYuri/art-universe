package yurykorzun.art.universe.music.data.raw.lastfm.repository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import yurykorzun.art.universe.common.persistence.entity.TaskStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.task.entity.LastfmTask;
import yurykorzun.art.universe.music.data.raw.lastfm.task.entity.LastfmTaskType;
import yurykorzun.art.universe.music.data.raw.lastfm.task.repository.LastfmTaskRepository;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.transaction.Transactional;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static yurykorzun.art.universe.music.data.raw.lastfm.common.utils.TimeTestUtils.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
public class LastfmTaskRepositoryTest {

    @Autowired
    private LastfmTaskRepository taskRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    public void givenValidLastfmTask_whenSaved_thenPersistedCorrectly() {
        // given
        Instant dueDttm = now().plus(1, ChronoUnit.MINUTES);
        LastfmTask request = LastfmTask.builder()
                .type(LastfmTaskType.TAGS_TOP_TAGS)
                .dueDttm(dueDttm)
            .build();

        // when
        request = taskRepository.save(request);

        // then
        Optional<LastfmTask> optPersisted = taskRepository.findById(request.getId());
        assertTrue(optPersisted.isPresent());
        LastfmTask persisted = optPersisted.get();
        assertEquals(request.getId(), persisted.getId());
        assertEquals(request.getType(), persisted.getType());
        assertEquals(dueDttm, persisted.getDueDttm());
        assertEquals(persisted.getStatus(), request.getStatus());
        assertEquals(TaskStatus.CREATED, request.getStatus());

        assertEquals(1, taskRepository.count());
    }

    @Test
    @Transactional
    public void givenEmptyTable_whenTagsRequestInserted_thenTagsRequestHistoryUpdated() {
        // given
        Instant dueDttm = now().plus(1, ChronoUnit.MINUTES);
        LastfmTask request = LastfmTask.builder()
                .type(LastfmTaskType.TAGS_TOP_TAGS)
                .dueDttm(dueDttm)
            .build();

        // when
        taskRepository.save(request);
        entityManager.flush();

        // then
        Query query = entityManager.createNativeQuery("""
            SELECT  COUNT(*)
            FROM    task_history
            WHERE   task_id = :id
        """);
        query.setParameter("id", request.getId());
        Number count = (Number) query.getSingleResult();

        assertEquals(1, count.intValue());
    }
}
