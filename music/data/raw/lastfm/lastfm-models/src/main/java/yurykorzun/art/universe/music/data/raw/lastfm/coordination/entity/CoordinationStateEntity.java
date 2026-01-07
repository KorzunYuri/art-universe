package yurykorzun.art.universe.music.data.raw.lastfm.coordination.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Entity representing global coordination state.
 * Singleton table with a single row (id=1).
 */
@Entity
@Table(name = "coordination_state", schema = "mu_raw_lastfm")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoordinationStateEntity {

    @Id
    @Column(name = "id")
    private Integer id;

    @Column(name = "status", nullable = false)
    @Convert(converter = CoordinationStatusConverter.class)
    @Builder.Default
    private CoordinationStatus status = CoordinationStatus.NORMAL;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "updated_by_instance", length = 100)
    private String updatedByInstance;
}
