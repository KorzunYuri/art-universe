package yurykorzun.art.universe.music.quiz.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import yurykorzun.art.universe.common.persistence.entity.BaseEntity;

import java.time.Instant;

@Entity(name = "pipeline_run")
@SuperBuilder
@NoArgsConstructor
@Getter @Setter
public class PipelineRun extends BaseEntity {

    @Id
    @SequenceGenerator(
        name = "pipeline_run_seq_gen",
        sequenceName = "pipeline_run_seq",
        allocationSize = 1
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pipeline_run_seq_gen")
    @Setter(value = AccessLevel.NONE)
    private Long id;

    @Column(name = "pipeline_id", nullable = false)
    private Long pipelineId;

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
}
