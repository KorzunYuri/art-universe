package yurykorzun.art.universe.music.data.raw.lastfm.task.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import yurykorzun.art.universe.common.data.raw.task.entity.TaskStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.common.archetypes.JpaOnlyTest;
import yurykorzun.art.universe.music.data.raw.lastfm.task.entity.LastfmTask;
import yurykorzun.art.universe.music.data.raw.lastfm.task.entity.LastfmTaskType;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static yurykorzun.art.universe.music.data.raw.lastfm.common.utils.TimeTestUtils.now;

@Tag("integration")
public class LastfmTaskRepositoryTest extends JpaOnlyTest {

    @Autowired
    private LastfmTaskRepository taskRepository;

    @Autowired
    private EntityManager entityManager;

    @AfterEach
    public void cleanDb() {
        taskRepository.deleteAll();
    }

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
