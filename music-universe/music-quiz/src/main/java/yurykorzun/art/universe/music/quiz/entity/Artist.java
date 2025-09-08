package yurykorzun.art.universe.music.quiz.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import yurykorzun.art.universe.common.persistence.entity.BaseEntity;

/**
 * Entity representing an artist approved for quiz participation
 */
@Entity(name = "artist")
@Getter
@Setter
public class Artist extends BaseEntity {

    @Id
    @SequenceGenerator(
        name = "artist_seq_gen",
        sequenceName = "artist_seq",
        allocationSize = 1
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "artist_seq_gen")
    @Setter(value = AccessLevel.NONE)
    private long id;

    /**
     * Reference to the id in mu.artist table
     */
    @Column(name = "master_id", nullable = false, unique = true)
    private Long masterId;
}
