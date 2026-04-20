package yurykorzun.art.universe.music.data.semantic.applicator.applier;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yurykorzun.art.universe.music.data.semantic.applicator.repository.ProposalRepository;
import yurykorzun.art.universe.music.data.semantic.model.ProposalType;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
public class ProposalApplier {

    private static final Logger log = LoggerFactory.getLogger(ProposalApplier.class);

    private final DependencyResolver dependencyResolver;
    private final ObjectMapper objectMapper;
    private final ProposalRepository proposalRepository;
    private final Map<ProposalType, ProposalApplyStrategy> strategies;

    public ProposalApplier(
        DependencyResolver dependencyResolver,
        ObjectMapper objectMapper,
        ProposalRepository proposalRepository,
        List<ProposalApplyStrategy> strategyBeans
    ) {
        this.dependencyResolver = dependencyResolver;
        this.objectMapper = objectMapper;
        this.proposalRepository = proposalRepository;
        this.strategies = new EnumMap<>(ProposalType.class);
        for (ProposalApplyStrategy strategy : strategyBeans) {
            this.strategies.put(strategy.supportedType(), strategy);
        }
    }

    @Transactional
    public void applyApprovedProposals(List<ProposalRow> proposals) {
        List<ProposalRow> sorted = dependencyResolver.topologicalSort(proposals);
        ApplicationContext context = new ApplicationContext();

        for (ProposalRow proposal : sorted) {
            String appliedRef = applyProposal(proposal, context);
            proposalRepository.markApplied(proposal.getId(), appliedRef);
            log.info("Applied proposal {} (type={}, synth_id={})",
                proposal.getId(), proposal.getProposalType(), proposal.getSynthId());
        }
    }

    private String applyProposal(ProposalRow proposal, ApplicationContext context) {
        JsonNode payload = parsePayload(proposal);

        ProposalApplyStrategy strategy = strategies.get(proposal.getProposalType());
        if (strategy == null) {
            log.warn("No strategy registered for proposal type {} (proposal id={}). Skipping.",
                proposal.getProposalType(), proposal.getId());
            return "type_not_implemented:" + proposal.getProposalType().getName();
        }

        try {
            return strategy.apply(payload, proposal, context);
        } catch (RuntimeException e) {
            log.error("Strategy for {} failed on proposal id={}: {}", proposal.getProposalType(), proposal.getId(), e.getMessage());
            throw e;
        }
    }

    private JsonNode parsePayload(ProposalRow proposal) {
        try {
            return objectMapper.readTree(proposal.getPayload());
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to parse payload of proposal id=" + proposal.getId(), e);
        }
    }
}
