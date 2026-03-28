package yurykorzun.art.universe.music.data.semantic.parser.parser;

import lombok.Data;

@Data
public class ParsedProposal {

    private String synthId;
    private String type;
    private short confidence;
    private String reasoning;
    private String payload;
}
