package yurykorzun.art.universe.music.data.semantic.applicator.applier.strategy;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.music.data.semantic.applicator.applier.ApplicationContext;
import yurykorzun.art.universe.music.data.semantic.applicator.applier.ProposalApplyStrategy;
import yurykorzun.art.universe.music.data.semantic.applicator.applier.ProposalRow;
import yurykorzun.art.universe.music.data.semantic.applicator.repository.MasterDataRepository;
import yurykorzun.art.universe.music.data.semantic.model.ProposalType;

@Component
public class CreateCategoryStrategy implements ProposalApplyStrategy {

    private final MasterDataRepository masterDataRepository;

    public CreateCategoryStrategy(MasterDataRepository masterDataRepository) {
        this.masterDataRepository = masterDataRepository;
    }

    @Override
    public ProposalType supportedType() {
        return ProposalType.CREATE_CATEGORY;
    }

    @Override
    public String apply(JsonNode payload, ProposalRow proposal, ApplicationContext context) {
        String name = payload.path("name").asText();
        if (name.isBlank()) {
            throw new IllegalArgumentException("CREATE_CATEGORY payload requires a non-blank 'name'");
        }
        Long existingId = masterDataRepository.findCategoryByName(name);
        Long categoryId = existingId != null ? existingId : masterDataRepository.createCategory(name);
        context.registerSynthId(proposal.getSynthId(), categoryId);
        return "category:" + categoryId;
    }
}
