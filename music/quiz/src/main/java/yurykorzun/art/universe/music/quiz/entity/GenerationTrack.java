package yurykorzun.art.universe.music.quiz.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import yurykorzun.art.universe.common.domain.entity.BaseEntity;

/**
 * Entity representing a track in a generation with details
 */
@Entity(name = "generation_track")
@SuperBuilder
@NoArgsConstructor
@Getter @Setter
public class GenerationTrack extends BaseEntity {

    @Id
    @SequenceGenerator(
        name = "generation_track_seq_gen",
        sequenceName = "generation_track_seq",
        allocationSize = 50
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "generation_track_seq_gen")
    @Setter(value = AccessLevel.NONE)
    private Long id;

    @Column(name = "generation_id", nullable = false)
    private Long generationId;

    @Column(name = "track_id", nullable = false)
    private Long trackId;

    @Column(name = "primary_artist_id", nullable = false)
    private Long primaryArtistId;

    @Column(name = "track_name", nullable = false, length = 500)
    private String trackName;

    @Column(name = "artist_name", nullable = false, length = 500)
    private String artistName;

    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;
}
