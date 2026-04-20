package yurykorzun.art.universe.music.data.semantic.applicator.applier;

import lombok.Data;
import yurykorzun.art.universe.common.domain.entity.MasterEntityType;
import yurykorzun.art.universe.music.data.semantic.model.ProposalResolution;
import yurykorzun.art.universe.music.data.semantic.model.ProposalType;

import java.util.UUID;

@Data
public class ProposalRow {

    private Long id;
    private UUID requestId;
    private String synthId;
    private ProposalType proposalType;
    private MasterEntityType subjectType;
    private Long subjectId;
    private String subjectRef;
    private short confidence;
    private String reasoning;
    private String payload;
    private ProposalResolution resolution;
}
