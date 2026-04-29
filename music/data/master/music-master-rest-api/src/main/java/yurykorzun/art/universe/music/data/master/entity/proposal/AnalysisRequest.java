package yurykorzun.art.universe.music.data.master.entity.proposal;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import yurykorzun.art.universe.music.data.master.model.DataSource;
import yurykorzun.art.universe.music.data.master.model.attribute.AttributeTargetType;
import yurykorzun.art.universe.music.data.semantic.model.AnalysisRequestStatus;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "analysis_request", schema = "mu_semantic_analysis")
@SuperBuilder
@NoArgsConstructor
@Getter
@Setter
public class AnalysisRequest {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "ticket_id", nullable = false)
    private UUID ticketId;

    @Column(name = "input_hash", nullable = false, length = 64)
    private String inputHash;

    @Column(name = "analysis_version", nullable = false, length = 32)
    private String analysisVersion;

    @Column(name = "data_source", nullable = false)
    private DataSource dataSource;

    @Column(name = "subject_type", nullable = false)
    private AttributeTargetType subjectType;

    @Column(name = "subject_id")
    private Long subjectId;

    @Column(name = "status", nullable = false)
    private AnalysisRequestStatus status;

    @Column(name = "llm_provider", length = 32)
    private String llmProvider;

    @Column(name = "llm_model", length = 64)
    private String llmModel;

    @Column(name = "prompt_tokens")
    private Integer promptTokens;

    @Column(name = "completion_tokens")
    private Integer completionTokens;

    @Column(name = "raw_response")
    private String rawResponse;

    @Column(name = "error_message")
    private String errorMessage;

    @Builder.Default
    @Column(name = "created_at", updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "completed_at")
    private Instant completedAt;
}
