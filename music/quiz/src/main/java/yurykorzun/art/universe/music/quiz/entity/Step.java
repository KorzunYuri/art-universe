package yurykorzun.art.universe.music.quiz.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import yurykorzun.art.universe.common.persistence.entity.BaseEntity;

@Entity(name = "step")
@SuperBuilder
@NoArgsConstructor
@Getter @Setter
public class Step extends BaseEntity {

    @Id
    @SequenceGenerator(
        name = "step_seq_gen",
        sequenceName = "step_seq",
        allocationSize = 1
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "step_seq_gen")
    @Setter(value = AccessLevel.NONE)
    private Long id;

    @Column(name = "type", nullable = false)
    @Convert(converter = StepTypeConverter.class)
    private StepType type;

    @Column(name = "alg_version", nullable = false)
    private Integer algVersion;

    @Column(name = "cfg_data", columnDefinition = "jsonb")
    private String cfgData;

    @Column(name = "preview_data", columnDefinition = "jsonb")
    private String previewData;

    @Column(name = "last_step_run_id")
    private Long lastStepRunId;

    @Builder.Default
    @Column(name = "deleted", nullable = false)
    private Boolean deleted = false;

    @Builder.Default
    @Column(name = "immutable", nullable = false)
    private Boolean immutable = false;
}
