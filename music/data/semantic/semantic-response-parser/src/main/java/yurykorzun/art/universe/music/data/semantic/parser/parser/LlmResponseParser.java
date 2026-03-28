package yurykorzun.art.universe.music.data.semantic.parser.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class LlmResponseParser {

    private static final Logger log = LoggerFactory.getLogger(LlmResponseParser.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<ParsedProposal> parse(String rawJson) {
        List<ParsedProposal> proposals = new ArrayList<>();

        try {
            JsonNode root = objectMapper.readTree(rawJson);
            JsonNode proposalsNode = root.path("proposals");

            if (!proposalsNode.isArray()) {
                log.warn("LLM response does not contain proposals array");
                return proposals;
            }

            for (JsonNode node : proposalsNode) {
                try {
                    ParsedProposal proposal = new ParsedProposal();
                    proposal.setSynthId(node.path("synth_id").asText(null));
                    proposal.setType(node.path("type").asText());
                    proposal.setConfidence((short) node.path("confidence").asInt(0));
                    proposal.setReasoning(node.path("reasoning").asText(""));
                    proposal.setPayload(objectMapper.writeValueAsString(node.path("payload")));
                    proposals.add(proposal);
                } catch (Exception e) {
                    log.warn("Failed to parse individual proposal", e);
                }
            }
        } catch (Exception e) {
            log.error("Failed to parse LLM response JSON", e);
        }

        return proposals;
    }
}
