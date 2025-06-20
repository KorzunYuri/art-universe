package yurykorzun.art.universe.music.quiz.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import yurykorzun.art.universe.common.persistence.entity.BaseEntity;

/**
 * Entity representing a track approved for quiz participation
 */
@Entity
@Table(indexes = {
    @Index(name = "idx_track_reference_id", columnList = "reference_id")
})
@Getter
@Setter
public class Track extends BaseEntity {

    @Id
    @SequenceGenerator(
        name = "track_seq_gen",
        sequenceName = "track_seq",
        allocationSize = 50
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "track_seq_gen")
    @Setter(value = AccessLevel.NONE)
    private long id;

    /**
     * Reference to the id in mu.track table
     */
    @Column(name = "reference_id", nullable = false)
    private Long referenceId;
}
