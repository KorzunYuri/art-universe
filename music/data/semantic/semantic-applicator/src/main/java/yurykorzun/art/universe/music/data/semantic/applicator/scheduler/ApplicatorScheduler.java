package yurykorzun.art.universe.music.data.semantic.applicator.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import yurykorzun.art.universe.common.config.client.ConfigPropertyHolder;
import yurykorzun.art.universe.music.data.semantic.applicator.applier.ProposalApplier;
import yurykorzun.art.universe.music.data.semantic.applicator.applier.ProposalRow;
import yurykorzun.art.universe.music.data.semantic.applicator.config.SemanticApplicatorProperty;
import yurykorzun.art.universe.music.data.semantic.applicator.threshold.ConfidenceThresholdService;
import yurykorzun.art.universe.music.data.semantic.model.ProposalResolution;

import java.util.List;
import java.util.UUID;

@Component
public class ApplicatorScheduler {

    private static final Logger log = LoggerFactory.getLogger(ApplicatorScheduler.class);

    private final JdbcTemplate jdbcTemplate;
    private final ConfidenceThresholdService thresholdService;
    private final ProposalApplier applier;
    private final ConfigPropertyHolder configPropertyHolder;

    public ApplicatorScheduler(
        JdbcTemplate jdbcTemplate,
        ConfidenceThresholdService thresholdService,
        ProposalApplier applier,
        ConfigPropertyHolder configPropertyHolder
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.thresholdService = thresholdService;
        this.applier = applier;
        this.configPropertyHolder = configPropertyHolder;
    }

    @Transactional
    public void evaluateAndApply() {
        int batchSize = configPropertyHolder.getInt(SemanticApplicatorProperty.BATCH_SIZE);

        // First: auto-evaluate pending proposals by confidence
        List<ProposalRow> pending = fetchPendingProposals(batchSize);
        for (ProposalRow proposal : pending) {
            ProposalResolution decision = thresholdService.evaluate(proposal.getConfidence());
            if (decision == ProposalResolution.AUTO_APPROVED || decision == ProposalResolution.AUTO_DECLINED) {
                jdbcTemplate.update(
                    "UPDATE mu_semantic_analysis.proposal SET resolution = ?, resolved_by = 'auto', resolved_at = CURRENT_TIMESTAMP WHERE id = ?",
                    decision.getCode(), proposal.getId()
                );
            }
        }

        // Second: apply auto-approved + manually approved proposals
        List<ProposalRow> approved = fetchApprovedProposals(batchSize);
        if (!approved.isEmpty()) {
            log.info("Applying {} approved proposals", approved.size());
            applier.applyApprovedProposals(approved);
        }
    }

    private List<ProposalRow> fetchPendingProposals(int batchSize) {
        return jdbcTemplate.query(
            "SELECT id, request_id, synth_id, proposal_type, subject_type, subject_id, subject_ref, confidence, reasoning, payload, resolution " +
            "FROM mu_semantic_analysis.proposal WHERE resolution = 1 ORDER BY created_at LIMIT ? FOR UPDATE SKIP LOCKED",
            (rs, rowNum) -> mapRow(rs),
            batchSize
        );
    }

    private List<ProposalRow> fetchApprovedProposals(int batchSize) {
        return jdbcTemplate.query(
            "SELECT id, request_id, synth_id, proposal_type, subject_type, subject_id, subject_ref, confidence, reasoning, payload, resolution " +
            "FROM mu_semantic_analysis.proposal WHERE resolution IN (2, 5) AND applied_ref IS NULL ORDER BY created_at LIMIT ? FOR UPDATE SKIP LOCKED",
            (rs, rowNum) -> mapRow(rs),
            batchSize
        );
    }

    private ProposalRow mapRow(java.sql.ResultSet rs) throws java.sql.SQLException {
        ProposalRow row = new ProposalRow();
        row.setId(rs.getLong("id"));
        row.setRequestId(UUID.fromString(rs.getString("request_id")));
        row.setSynthId(rs.getString("synth_id"));
        row.setProposalType(rs.getInt("proposal_type"));
        row.setSubjectType(rs.getInt("subject_type"));
        long subjId = rs.getLong("subject_id");
        row.setSubjectId(rs.wasNull() ? null : subjId);
        row.setSubjectRef(rs.getString("subject_ref"));
        row.setConfidence(rs.getShort("confidence"));
        row.setReasoning(rs.getString("reasoning"));
        row.setPayload(rs.getString("payload"));
        row.setResolution(rs.getInt("resolution"));
        return row;
    }
}
