package yurykorzun.art.universe.music.quiz.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import yurykorzun.art.universe.common.domain.entity.BaseEntity;

import java.time.Instant;

@Entity(name = "step_run")
@SuperBuilder
@NoArgsConstructor
@Getter @Setter
public class StepRun extends BaseEntity {

    @Id
    @SequenceGenerator(
        name = "step_run_seq_gen",
        sequenceName = "step_run_seq",
        allocationSize = 1
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "step_run_seq_gen")
    @Setter(value = AccessLevel.NONE)
    private Long id;

    @Column(name = "pipeline_run_id")
    private Long pipelineRunId;

    @Column(name = "step_id", nullable = false)
    private Long stepId;

    @Column(name = "step_type", nullable = false)
    @Convert(converter = StepTypeConverter.class)
    private StepType stepType;

    @Column(name = "alg_version", nullable = false)
    private Integer algVersion;

    @Column(name = "step_cfg_data", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String stepCfgData;

    @Column(name = "status", nullable = false)
    @Convert(converter = ExecutionStatusConverter.class)
    @Builder.Default
    private ExecutionStatus status = ExecutionStatus.PENDING;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "result_table_name")
    private String resultTableName;

    @Column(name = "input_table_name")
    private String inputTableName;

    @Column(name = "result_stats", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String resultStats;
}
