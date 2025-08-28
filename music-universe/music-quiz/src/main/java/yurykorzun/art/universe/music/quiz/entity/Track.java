package yurykorzun.art.universe.music.quiz.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import yurykorzun.art.universe.common.persistence.entity.BaseEntity;

/**
 * Entity representing a track approved for quiz participation
 */
@Entity(name = "track")
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
    @Column(name = "master_id", nullable = false, unique = true)
    private Long masterId;
}
