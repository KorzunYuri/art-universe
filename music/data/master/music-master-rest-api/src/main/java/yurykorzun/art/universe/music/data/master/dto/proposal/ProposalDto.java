package yurykorzun.art.universe.music.data.master.dto.proposal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import yurykorzun.art.universe.music.data.master.entity.attribute.AttributeTargetType;
import yurykorzun.art.universe.music.data.semantic.model.ProposalResolution;
import yurykorzun.art.universe.music.data.semantic.model.ProposalType;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProposalDto {

    private Long id;
    private UUID requestId;
    private String synthId;
    private ProposalType proposalType;
    private AttributeTargetType subjectType;
    private Long subjectId;
    private String subjectRef;
    private Short confidence;
    private String reasoning;
    private String payload;
    private ProposalResolution resolution;
    private String resolvedBy;
    private Instant resolvedAt;
    private String appliedRef;
    private Instant createdAt;
}
