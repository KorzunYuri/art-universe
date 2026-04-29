package yurykorzun.art.universe.music.data.master.entity.proposal;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import yurykorzun.art.universe.music.data.master.model.attribute.AttributeTargetType;
import yurykorzun.art.universe.music.data.semantic.model.ProposalResolution;
import yurykorzun.art.universe.music.data.semantic.model.ProposalType;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "proposal", schema = "mu_semantic_analysis")
@SuperBuilder
@NoArgsConstructor
@Getter
@Setter
public class Proposal {

    @Id
    @SequenceGenerator(
        name = "proposal_seq_gen",
        sequenceName = "mu_semantic_analysis.proposal_seq",
        allocationSize = 50
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "proposal_seq_gen")
    @Setter(AccessLevel.NONE)
    private Long id;

    @Column(name = "request_id", nullable = false)
    private UUID requestId;

    @Column(name = "synth_id", length = 16)
    private String synthId;

    @Column(name = "proposal_type", nullable = false)
    private ProposalType proposalType;

    @Column(name = "subject_type", nullable = false)
    private AttributeTargetType subjectType;

    @Column(name = "subject_id")
    private Long subjectId;

    @Column(name = "subject_ref", length = 16)
    private String subjectRef;

    @Column(name = "confidence", nullable = false)
    private Short confidence;

    @Column(name = "reasoning", nullable = false)
    private String reasoning;

    @Column(name = "payload", nullable = false, columnDefinition = "jsonb")
    private String payload;

    @Column(name = "resolution", nullable = false)
    private ProposalResolution resolution;

    @Column(name = "resolved_by", length = 64)
    private String resolvedBy;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    @Column(name = "applied_ref", length = 256)
    private String appliedRef;

    @Builder.Default
    @Column(name = "created_at", updatable = false)
    private Instant createdAt = Instant.now();
}
