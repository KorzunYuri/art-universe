package yurykorzun.art.universe.music.data.semantic.applicator.applier.strategy;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.common.domain.entity.MasterEntityType;
import yurykorzun.art.universe.music.data.semantic.applicator.applier.ApplicationContext;
import yurykorzun.art.universe.music.data.semantic.applicator.applier.ProposalApplyStrategy;
import yurykorzun.art.universe.music.data.semantic.applicator.applier.ProposalRow;
import yurykorzun.art.universe.music.data.semantic.applicator.applier.support.ProposalPayloads;
import yurykorzun.art.universe.music.data.semantic.applicator.repository.MasterDataRepository;
import yurykorzun.art.universe.music.data.semantic.model.ProposalType;

@Component
public class CreateEntityStrategy implements ProposalApplyStrategy {

    private final MasterDataRepository masterDataRepository;

    public CreateEntityStrategy(MasterDataRepository masterDataRepository) {
        this.masterDataRepository = masterDataRepository;
    }

    @Override
    public ProposalType supportedType() {
        return ProposalType.CREATE_ENTITY;
    }

    @Override
    public String apply(JsonNode payload, ProposalRow proposal, ApplicationContext context) {
        MasterEntityType entityType = MasterEntityType.fromString(
            ProposalPayloads.requireString(payload, "entity_type", proposalTypeName())
        );
        String name = ProposalPayloads.requireString(payload, "name", proposalTypeName());
        if (entityType == MasterEntityType.PERSON) {
            throw new IllegalArgumentException(
                "CREATE_ENTITY for PERSON is not supported yet — PERSON lives in the art schema "
                    + "and cross-schema creation is not wired. The LLM must reference existing persons by id.");
        }
        Long newId = masterDataRepository.createEntity(entityType, name);
        context.registerSynthId(proposal.getSynthId(), newId);
        return entityType.getName() + ":" + newId;
    }
}
