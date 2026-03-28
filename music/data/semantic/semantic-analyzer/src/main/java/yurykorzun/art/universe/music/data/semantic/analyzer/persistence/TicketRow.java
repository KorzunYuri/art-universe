package yurykorzun.art.universe.music.data.semantic.analyzer.persistence;

import lombok.Data;

import java.util.UUID;

@Data
public class TicketRow {

    private UUID id;
    private int dataSource;
    private int subjectType;
    private Long subjectId;
    private String subjectName;
    private String textSamplesJson;
    private int[] expectedProposalTypes;
    private int[] expectedEntityTypes;
}
