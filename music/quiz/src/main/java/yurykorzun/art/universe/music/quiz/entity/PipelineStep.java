package yurykorzun.art.universe.music.quiz.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import yurykorzun.art.universe.common.domain.entity.BaseEntity;

@Entity(name = "pipeline_step")
@SuperBuilder
@NoArgsConstructor
@Getter @Setter
public class PipelineStep extends BaseEntity {

    @Id
    @SequenceGenerator(
        name = "pipeline_step_seq_gen",
        sequenceName = "pipeline_step_seq",
        allocationSize = 1
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pipeline_step_seq_gen")
    @Setter(value = AccessLevel.NONE)
    private Long id;

    @Column(name = "pipeline_id", nullable = false)
    private Long pipelineId;

    @Column(name = "step_id", nullable = false)
    private Long stepId;

    @Column(name = "ord", nullable = false)
    private Integer ord;
}
