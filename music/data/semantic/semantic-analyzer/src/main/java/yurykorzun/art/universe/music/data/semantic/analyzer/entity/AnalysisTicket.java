package yurykorzun.art.universe.music.data.semantic.analyzer.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import yurykorzun.art.universe.common.domain.entity.MasterEntityType;
import yurykorzun.art.universe.music.data.master.model.DataSource;
import yurykorzun.art.universe.music.data.semantic.model.AnalysisMode;
import yurykorzun.art.universe.music.data.semantic.model.AnalysisTicketStatus;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "analysis_ticket", schema = "mu_semantic_analysis")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalysisTicket {

    @Id
    @Column(name = "id")
    @Setter(AccessLevel.NONE)
    private UUID id;

    @Column(name = "data_source", nullable = false)
    private DataSource dataSource;

    @Column(name = "subject_type", nullable = false)
    private MasterEntityType subjectType;

    @Column(name = "subject_id")
    private Long subjectId;

    @Column(name = "subject_name", nullable = false, length = 256)
    private String subjectName;

    @Column(name = "text_samples", nullable = false, columnDefinition = "jsonb")
    private String textSamplesJson;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "expected_proposal_types", columnDefinition = "smallint[]")
    private Integer[] expectedProposalTypes;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "expected_entity_types", columnDefinition = "smallint[]")
    private Integer[] expectedEntityTypes;

    @Column(name = "analysis_mode", nullable = false)
    private AnalysisMode analysisMode;

    @Column(name = "status", nullable = false)
    private AnalysisTicketStatus status;

    @Column(name = "analysis_version", length = 32)
    private String analysisVersion;

    @Column(name = "error_message", columnDefinition = "text")
    private String errorMessage;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "picked_up_at")
    private Instant pickedUpAt;

    @Column(name = "completed_at")
    private Instant completedAt;
}
