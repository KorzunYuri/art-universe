package yurykorzun.art.universe.music.data.raw.spotify.etl.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity(name = "staging_iteration")
@Table(name = "staging_iteration")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class StagingIteration {

    @Id
    @SequenceGenerator(name = "staging_iteration_seq_gen", sequenceName = "staging_iteration_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "staging_iteration_seq_gen")
    @Setter(AccessLevel.NONE)
    private long id;

    @Column(name = "status", nullable = false)
    @Convert(converter = StagingIterationStatusConverter.class)
    @Builder.Default
    private StagingIterationStatus status = StagingIterationStatus.OPEN;

    @Column(name = "records_staged")
    @Builder.Default
    private Long recordsStaged = 0L;

    @Column(name = "records_applied")
    @Builder.Default
    private Long recordsApplied = 0L;

    @Column(name = "records_failed")
    @Builder.Default
    private Long recordsFailed = 0L;

    @Column(name = "opened_at", nullable = false)
    @Builder.Default
    private Instant openedAt = Instant.now();

    @Column(name = "sealed_at")
    private Instant sealedAt;

    @Column(name = "applied_at")
    private Instant appliedAt;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "retry_count")
    @Builder.Default
    private Short retryCount = 0;

    @Column(name = "last_retry_at")
    private Instant lastRetryAt;
}
