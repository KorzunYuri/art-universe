package yurykorzun.art.universe.music.data.raw.lastfm.coordination.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import yurykorzun.art.universe.music.data.raw.lastfm.coordination.entity.CoordinationInstanceEntity;

import java.time.Instant;
import java.util.List;

/**
 * Repository for coordination instances (application instances).
 */
@Repository
public interface CoordinationInstanceRepository extends JpaRepository<CoordinationInstanceEntity, String> {

    /**
     * Find instances with stale heartbeats (crashed instances).
     *
     * @param threshold instances with last_heartbeat before this threshold are considered stale
     * @return list of instance IDs that are stale
     */
    @Query("SELECT i.instanceId FROM CoordinationInstanceEntity i WHERE i.lastHeartbeat < :threshold")
    List<String> findStaleInstances(@Param("threshold") Instant threshold);

    /**
     * Update heartbeat timestamp for an instance.
     *
     * @param instanceId instance ID to update
     * @param heartbeat  new heartbeat timestamp
     * @return number of rows updated (should be 1)
     */
    @Modifying
    @Query("UPDATE CoordinationInstanceEntity i SET i.lastHeartbeat = :heartbeat WHERE i.instanceId = :instanceId")
    int updateHeartbeat(@Param("instanceId") String instanceId, @Param("heartbeat") Instant heartbeat);
}
