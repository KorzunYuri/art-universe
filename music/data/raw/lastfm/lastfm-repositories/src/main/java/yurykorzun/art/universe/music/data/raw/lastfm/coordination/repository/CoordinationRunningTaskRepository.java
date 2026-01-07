package yurykorzun.art.universe.music.data.raw.lastfm.coordination.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import yurykorzun.art.universe.music.data.raw.lastfm.coordination.entity.CoordinationRunningTaskEntity;

import java.time.Instant;

/**
 * Repository for coordination running tasks.
 */
@Repository
public interface CoordinationRunningTaskRepository extends JpaRepository<CoordinationRunningTaskEntity, String> {

    /**
     * Attempts to atomically acquire a task for execution.
     * Uses INSERT ... ON CONFLICT DO NOTHING for atomic check-and-set.
     *
     * @param taskKey    unique task identifier
     * @param instanceId instance attempting to acquire the task
     * @param acquiredAt timestamp of acquisition
     * @return number of rows inserted (1 if acquired, 0 if task already running)
     */
    @Modifying
    @Query(value = """
            INSERT INTO coordination_running_task (task_key, instance_id, acquired_at)
            VALUES (:taskKey, :instanceId, :acquiredAt)
            ON CONFLICT (task_key) DO NOTHING
            """, nativeQuery = true)
    int tryInsertTask(@Param("taskKey") String taskKey,
                      @Param("instanceId") String instanceId,
                      @Param("acquiredAt") Instant acquiredAt);
}
