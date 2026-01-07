package yurykorzun.art.universe.music.data.raw.lastfm.coordination.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Entity representing a coordinator instance (application instance).
 * Tracks instances for heartbeat monitoring and crash detection.
 */
@Entity
@Table(name = "coordination_instance", schema = "mu_raw_lastfm")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoordinationInstanceEntity {

    @Id
    @Column(name = "instance_id", length = 100)
    private String instanceId;

    @Column(name = "module_name", nullable = false, length = 50)
    private String moduleName;

    @Column(name = "host_name", length = 255)
    private String hostName;

    @Column(name = "start_time", nullable = false)
    private Instant startTime;

    @Column(name = "last_heartbeat", nullable = false)
    private Instant lastHeartbeat;
}
