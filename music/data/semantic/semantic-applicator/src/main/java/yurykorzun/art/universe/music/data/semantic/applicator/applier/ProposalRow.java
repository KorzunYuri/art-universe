package yurykorzun.art.universe.music.data.semantic.applicator.applier;

import lombok.Data;

import java.util.UUID;

@Data
public class ProposalRow {

    private Long id;
    private UUID requestId;
    private String synthId;
    private int proposalType;
    private int subjectType;
    private Long subjectId;
    private String subjectRef;
    private short confidence;
    private String reasoning;
    private String payload;
    private int resolution;
}
