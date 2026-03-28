package yurykorzun.art.universe.music.data.semantic.parser.writer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.music.data.semantic.model.ProposalType;
import yurykorzun.art.universe.music.data.semantic.parser.parser.ParsedProposal;

import java.util.List;
import java.util.UUID;

@Component
public class ProposalWriter {

    private static final Logger log = LoggerFactory.getLogger(ProposalWriter.class);

    private final JdbcTemplate jdbcTemplate;

    public ProposalWriter(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public int writeProposals(UUID requestId, int subjectType, Long subjectId, List<ParsedProposal> proposals) {
        int written = 0;

        for (ParsedProposal proposal : proposals) {
            try {
                ProposalType type = ProposalType.fromString(proposal.getType());

                jdbcTemplate.update(
                    """
                    INSERT INTO mu_semantic_analysis.proposal
                        (id, request_id, synth_id, proposal_type, subject_type, subject_id,
                         confidence, reasoning, payload, resolution)
                    VALUES (nextval('mu_semantic_analysis.proposal_seq'), ?, ?, ?, ?, ?, ?, ?, ?::jsonb, 1)
                    """,
                    requestId, proposal.getSynthId(), type.getCode(),
                    subjectType, subjectId,
                    proposal.getConfidence(), proposal.getReasoning(), proposal.getPayload()
                );
                written++;
            } catch (Exception e) {
                log.warn("Failed to write proposal synth_id={}: {}", proposal.getSynthId(), e.getMessage());
            }
        }

        log.info("Wrote {}/{} proposals for request {}", written, proposals.size(), requestId);
        return written;
    }
}
