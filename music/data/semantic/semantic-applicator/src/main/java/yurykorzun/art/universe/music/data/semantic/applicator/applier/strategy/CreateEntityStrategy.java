package yurykorzun.art.universe.music.data.semantic.applicator.applier.strategy;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.common.domain.entity.MasterEntityType;
import yurykorzun.art.universe.music.data.semantic.applicator.applier.ApplicationContext;
import yurykorzun.art.universe.music.data.semantic.applicator.applier.ProposalApplyStrategy;
import yurykorzun.art.universe.music.data.semantic.applicator.applier.ProposalRow;
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
        String entityTypeRaw = payload.path("entity_type").asText();
        String name = payload.path("name").asText();
        if (name.isBlank()) {
            throw new IllegalArgumentException("CREATE_ENTITY payload requires a non-blank 'name'");
        }
        MasterEntityType entityType = MasterEntityType.fromString(entityTypeRaw);
        Long newId = masterDataRepository.createEntity(entityType, name);
        context.registerSynthId(proposal.getSynthId(), newId);
        return entityType.getName() + ":" + newId;
    }
}
