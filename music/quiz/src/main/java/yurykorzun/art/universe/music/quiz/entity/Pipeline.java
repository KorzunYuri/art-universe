package yurykorzun.art.universe.music.quiz.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import yurykorzun.art.universe.common.persistence.entity.BaseEntity;

@Entity(name = "pipeline")
@SuperBuilder
@NoArgsConstructor
@Getter @Setter
public class Pipeline extends BaseEntity {

    @Id
    @SequenceGenerator(
        name = "pipeline_seq_gen",
        sequenceName = "pipeline_seq",
        allocationSize = 1
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pipeline_seq_gen")
    @Setter(value = AccessLevel.NONE)
    private Long id;

    @Column(name = "game_id", nullable = false)
    private Long gameId;

    @Builder.Default
    @Column(name = "immutable", nullable = false)
    private Boolean immutable = false;
}
