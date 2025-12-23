package yurykorzun.art.universe.music.data.raw.lastfm.coordination.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Entity representing a currently running task.
 * Enforces task deduplication across distributed instances.
 */
@Entity
@Table(name = "coordination_running_task", schema = "mu_raw_lastfm")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoordinationRunningTaskEntity {

    @Id
    @Column(name = "task_key", length = 100)
    private String taskKey;

    @Column(name = "instance_id", nullable = false, length = 100)
    private String instanceId;

    @Column(name = "acquired_at", nullable = false)
    private Instant acquiredAt;
}
