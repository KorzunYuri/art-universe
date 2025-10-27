package yurykorzun.art.universe.music.quiz.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import yurykorzun.art.universe.common.persistence.entity.BaseEntity;

/**
 * Entity representing a track generation for a quiz game
 */
@Entity(name = "generation")
@SuperBuilder
@NoArgsConstructor
@Getter @Setter
public class Generation extends BaseEntity {

    @Id
    @SequenceGenerator(
        name = "generation_seq_gen",
        sequenceName = "generation_seq",
        allocationSize = 1
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "generation_seq_gen")
    @Setter(value = AccessLevel.NONE)
    private Long id;

    @Column(name = "game_id", nullable = false)
    private Long gameId;

    @Column(name = "pipeline_id")
    private Long pipelineId;

    @Column(name = "pipeline_run_id")
    private Long pipelineRunId;

    @Column(name = "target_count", nullable = false)
    private Integer targetCount;

    @Column(name = "status", nullable = false)
    @Convert(converter = GenerationStatusConverter.class)
    private GenerationStatus status;

    @Column(name = "result_table_name", length = 100)
    private String resultTableName;

    @Builder.Default
    @Column(name = "approved", nullable = false)
    private Boolean approved = false;
}
