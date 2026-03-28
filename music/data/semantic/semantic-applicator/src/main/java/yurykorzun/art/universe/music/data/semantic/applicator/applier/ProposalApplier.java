package yurykorzun.art.universe.music.data.semantic.applicator.applier;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yurykorzun.art.universe.music.data.semantic.model.ProposalResolution;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProposalApplier {

    private static final Logger log = LoggerFactory.getLogger(ProposalApplier.class);

    private final JdbcTemplate jdbcTemplate;
    private final DependencyResolver dependencyResolver;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ProposalApplier(JdbcTemplate jdbcTemplate, DependencyResolver dependencyResolver) {
        this.jdbcTemplate = jdbcTemplate;
        this.dependencyResolver = dependencyResolver;
    }

    @Transactional
    public void applyApprovedProposals(List<ProposalRow> proposals) {
        List<ProposalRow> sorted = dependencyResolver.topologicalSort(proposals);
        Map<String, Long> synthIdMap = new HashMap<>();

        for (ProposalRow proposal : sorted) {
            try {
                String appliedRef = applyProposal(proposal, synthIdMap);
                markApplied(proposal.getId(), appliedRef);
                log.info("Applied proposal {} (type={}, synth_id={})",
                    proposal.getId(), proposal.getProposalType(), proposal.getSynthId());
            } catch (Exception e) {
                log.error("Failed to apply proposal {}, rolling back batch", proposal.getId(), e);
                throw new RuntimeException("Proposal application failed for id=" + proposal.getId(), e);
            }
        }
    }

    private String applyProposal(ProposalRow proposal, Map<String, Long> synthIdMap) {
        try {
            JsonNode payload = objectMapper.readTree(proposal.getPayload());

            return switch (proposal.getProposalType()) {
                case 1 -> applyCreateEntity(payload, proposal, synthIdMap);
                case 7 -> applyCreateCategory(payload, proposal, synthIdMap);
                default -> {
                    log.info("Proposal type {} not yet implemented for auto-application", proposal.getProposalType());
                    yield "type_not_implemented";
                }
            };
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse proposal payload", e);
        }
    }

    private String applyCreateEntity(JsonNode payload, ProposalRow proposal, Map<String, Long> synthIdMap) {
        String entityType = payload.path("entity_type").asText();
        String name = payload.path("name").asText();

        String table = switch (entityType.toLowerCase()) {
            case "artist" -> "artist";
            case "album" -> "album";
            case "track" -> "track";
            default -> throw new IllegalArgumentException("Unsupported entity type for creation: " + entityType);
        };

        String seqName = table + "_seq";
        Long newId = jdbcTemplate.queryForObject("SELECT nextval('mu." + seqName + "')", Long.class);

        jdbcTemplate.update(
            "INSERT INTO mu." + table + " (id, name, created_at, updated_at) VALUES (?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
            newId, name
        );

        if (proposal.getSynthId() != null) {
            synthIdMap.put(proposal.getSynthId(), newId);
        }

        return table + ":" + newId;
    }

    private String applyCreateCategory(JsonNode payload, ProposalRow proposal, Map<String, Long> synthIdMap) {
        String name = payload.path("name").asText();

        Long newId = jdbcTemplate.queryForObject("SELECT nextval('mu.category_seq')", Long.class);

        jdbcTemplate.update(
            "INSERT INTO mu.category (id, name, created_at, updated_at) VALUES (?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
            newId, name
        );

        if (proposal.getSynthId() != null) {
            synthIdMap.put(proposal.getSynthId(), newId);
        }

        return "category:" + newId;
    }

    private void markApplied(Long proposalId, String appliedRef) {
        jdbcTemplate.update(
            """
            UPDATE mu_semantic_analysis.proposal
            SET resolution = ?, applied_ref = ?, resolved_at = CURRENT_TIMESTAMP, resolved_by = 'applicator'
            WHERE id = ?
            """,
            ProposalResolution.APPROVED.getCode(), appliedRef, proposalId
        );
    }
}
