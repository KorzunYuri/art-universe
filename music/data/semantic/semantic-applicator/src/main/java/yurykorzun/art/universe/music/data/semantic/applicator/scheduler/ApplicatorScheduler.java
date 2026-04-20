package yurykorzun.art.universe.music.data.semantic.applicator.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.common.config.client.ConfigPropertyHolder;
import yurykorzun.art.universe.music.data.semantic.applicator.applier.ProposalApplier;
import yurykorzun.art.universe.music.data.semantic.applicator.applier.ProposalRow;
import yurykorzun.art.universe.music.data.semantic.applicator.config.SemanticApplicatorProperty;
import yurykorzun.art.universe.music.data.semantic.applicator.repository.ProposalRepository;
import yurykorzun.art.universe.music.data.semantic.applicator.threshold.ConfidenceThresholdService;
import yurykorzun.art.universe.music.data.semantic.model.ProposalResolution;

import java.util.List;

@Component
public class ApplicatorScheduler {

    private static final Logger log = LoggerFactory.getLogger(ApplicatorScheduler.class);

    private final ProposalRepository proposalRepository;
    private final ConfidenceThresholdService thresholdService;
    private final ProposalApplier applier;
    private final ConfigPropertyHolder configPropertyHolder;

    public ApplicatorScheduler(
        ProposalRepository proposalRepository,
        ConfidenceThresholdService thresholdService,
        ProposalApplier applier,
        ConfigPropertyHolder configPropertyHolder
    ) {
        this.proposalRepository = proposalRepository;
        this.thresholdService = thresholdService;
        this.applier = applier;
        this.configPropertyHolder = configPropertyHolder;
    }

    public void evaluateAndApply() {
        int batchSize = configPropertyHolder.getInt(SemanticApplicatorProperty.BATCH_SIZE);

        autoResolvePending(batchSize);
        applyApproved(batchSize);
    }

    private void autoResolvePending(int batchSize) {
        List<ProposalRow> pending = proposalRepository.fetchPendingForUpdate(batchSize);
        for (ProposalRow proposal : pending) {
            ProposalResolution decision = thresholdService.evaluate(proposal.getConfidence());
            if (decision == ProposalResolution.AUTO_APPROVED || decision == ProposalResolution.AUTO_DECLINED) {
                proposalRepository.markAutoResolution(proposal.getId(), decision);
            }
        }
    }

    private void applyApproved(int batchSize) {
        List<ProposalRow> approved = proposalRepository.fetchApprovedForUpdate(batchSize);
        if (approved.isEmpty()) {
            return;
        }
        log.info("Applying {} approved proposals", approved.size());
        applier.applyApprovedProposals(approved);
    }
}
