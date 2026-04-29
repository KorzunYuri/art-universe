package yurykorzun.art.universe.music.data.semantic.applicator.applier.strategy;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.music.data.semantic.applicator.applier.ApplicationContext;
import yurykorzun.art.universe.music.data.semantic.applicator.applier.ProposalApplyStrategy;
import yurykorzun.art.universe.music.data.semantic.applicator.applier.ProposalRow;
import yurykorzun.art.universe.music.data.semantic.applicator.applier.service.CategoryApplicationService;
import yurykorzun.art.universe.music.data.semantic.applicator.applier.support.ProposalPayloads;
import yurykorzun.art.universe.music.data.semantic.model.PayloadFields;
import yurykorzun.art.universe.music.data.semantic.model.ProposalType;

@Component
public class CreateCategoryStrategy implements ProposalApplyStrategy {

    private final CategoryApplicationService categoryService;

    public CreateCategoryStrategy(CategoryApplicationService categoryService) {
        this.categoryService = categoryService;
    }

    @Override
    public ProposalType supportedType() {
        return ProposalType.CREATE_CATEGORY;
    }

    @Override
    public String apply(JsonNode payload, ProposalRow proposal, ApplicationContext context) {
        String name = ProposalPayloads.requireString(payload, PayloadFields.NAME, proposalTypeName());
        CategoryApplicationService.Resolved resolved = categoryService.findOrCreate(name);
        context.registerSynthId(proposal.getSynthId(), resolved.id());
        return "category:" + resolved.id() + (resolved.existed() ? ":existing" : ":created");
    }
}
