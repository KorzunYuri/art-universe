package yurykorzun.art.universe.music.data.semantic.applicator.threshold;

import org.springframework.stereotype.Service;
import yurykorzun.art.universe.common.config.client.ConfigPropertyHolder;
import yurykorzun.art.universe.music.data.semantic.applicator.config.SemanticApplicatorProperty;
import yurykorzun.art.universe.music.data.semantic.model.ProposalResolution;

@Service
public class ConfidenceThresholdService {

    private final ConfigPropertyHolder configPropertyHolder;

    public ConfidenceThresholdService(ConfigPropertyHolder configPropertyHolder) {
        this.configPropertyHolder = configPropertyHolder;
    }

    public ProposalResolution evaluate(short confidence) {
        int autoApproveThreshold = configPropertyHolder.getInt(SemanticApplicatorProperty.AUTO_APPROVE_THRESHOLD);
        int autoDeclineThreshold = configPropertyHolder.getInt(SemanticApplicatorProperty.AUTO_DECLINE_THRESHOLD);

        if (confidence >= autoApproveThreshold) {
            return ProposalResolution.AUTO_APPROVED;
        }
        if (confidence < autoDeclineThreshold) {
            return ProposalResolution.AUTO_DECLINED;
        }
        return ProposalResolution.PENDING;
    }
}
