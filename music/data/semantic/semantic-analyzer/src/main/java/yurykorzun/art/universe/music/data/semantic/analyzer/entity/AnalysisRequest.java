package yurykorzun.art.universe.music.data.semantic.analyzer.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import yurykorzun.art.universe.common.domain.entity.MasterEntityType;
import yurykorzun.art.universe.common.persistence.converter.EntityTypeConverter;
import yurykorzun.art.universe.music.data.master.model.DataSource;
import yurykorzun.art.universe.music.data.master.model.DataSourceConverter;
import yurykorzun.art.universe.music.data.semantic.model.AnalysisMode;
import yurykorzun.art.universe.music.data.semantic.model.AnalysisModeConverter;
import yurykorzun.art.universe.music.data.semantic.model.AnalysisRequestStatus;
import yurykorzun.art.universe.music.data.semantic.model.AnalysisRequestStatusConverter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "analysis_request", schema = "mu_semantic_analysis")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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

    @Convert(converter = DataSourceConverter.class)
    @Column(name = "data_source", nullable = false)
    private DataSource dataSource;

    @Convert(converter = EntityTypeConverter.class)
    @Column(name = "subject_type", nullable = false)
    private MasterEntityType subjectType;

    @Column(name = "subject_id")
    private Long subjectId;

    @Convert(converter = AnalysisModeConverter.class)
    @Column(name = "analysis_mode", nullable = false)
    private AnalysisMode analysisMode;

    @Convert(converter = AnalysisRequestStatusConverter.class)
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

    @Column(name = "raw_response", columnDefinition = "text")
    private String rawResponse;

    @Column(name = "error_message", columnDefinition = "text")
    private String errorMessage;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "completed_at")
    private Instant completedAt;
}
