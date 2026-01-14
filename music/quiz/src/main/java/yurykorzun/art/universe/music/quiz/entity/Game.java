package yurykorzun.art.universe.music.quiz.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import yurykorzun.art.universe.common.domain.entity.BaseEntity;

/**
 * Entity representing a quiz game
 */
@Entity(name = "game")
@SuperBuilder
@NoArgsConstructor
@Getter @Setter
public class Game extends BaseEntity {

    @Id
    @SequenceGenerator(
        name = "game_seq_gen",
        sequenceName = "game_seq",
        allocationSize = 1
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "game_seq_gen")
    @Setter(value = AccessLevel.NONE)
    private Long id;

    @Column(name = "pipeline_id", nullable = false)
    private Long pipelineId;
}
