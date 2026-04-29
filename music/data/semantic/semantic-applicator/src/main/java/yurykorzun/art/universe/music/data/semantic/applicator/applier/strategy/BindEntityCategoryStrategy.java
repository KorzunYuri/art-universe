package yurykorzun.art.universe.music.data.semantic.applicator.applier.strategy;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.common.domain.entity.MasterEntityType;
import yurykorzun.art.universe.music.data.semantic.applicator.applier.ApplicationContext;
import yurykorzun.art.universe.music.data.semantic.applicator.applier.ProposalApplyStrategy;
import yurykorzun.art.universe.music.data.semantic.applicator.applier.ProposalRow;
import yurykorzun.art.universe.music.data.semantic.applicator.applier.service.CategoryApplicationService;
import yurykorzun.art.universe.music.data.semantic.applicator.applier.support.EntityReferenceResolver;
import yurykorzun.art.universe.music.data.semantic.applicator.applier.support.ProposalPayloads;
import yurykorzun.art.universe.music.data.semantic.applicator.repository.MasterDataRepository;
import yurykorzun.art.universe.music.data.semantic.model.ProposalType;

@Component
public class BindEntityCategoryStrategy implements ProposalApplyStrategy {

    private static final String PROPOSAL_TYPE = "BIND_ENTITY_CATEGORY";

    private final MasterDataRepository masterDataRepository;
    private final CategoryApplicationService categoryService;
    private final EntityReferenceResolver entityResolver;

    public BindEntityCategoryStrategy(
        MasterDataRepository masterDataRepository,
        CategoryApplicationService categoryService,
        EntityReferenceResolver entityResolver
    ) {
        this.masterDataRepository = masterDataRepository;
        this.categoryService = categoryService;
        this.entityResolver = entityResolver;
    }

    @Override
    public ProposalType supportedType() {
        return ProposalType.BIND_ENTITY_CATEGORY;
    }

    @Override
    public String apply(JsonNode payload, ProposalRow proposal, ApplicationContext context) {
        MasterEntityType entityType = MasterEntityType.fromString(
            ProposalPayloads.requireString(payload, "entity_type", supportedType().name())
        );
        Long entityId = entityResolver.require(payload, "entity_id", "entity_ref", context, PROPOSAL_TYPE, "entity");

        Long categoryId = entityResolver.resolveOrNull(
            payload, "category_id", "category_ref", context, PROPOSAL_TYPE, "category"
        );
        if (categoryId == null) {
            String categoryName = ProposalPayloads.optionalString(payload, "category_name");
            if (categoryName == null) {
                throw new IllegalArgumentException(
                    PROPOSAL_TYPE + " requires category_id, category_ref, or category_name"
                );
            }
            categoryId = categoryService.findOrCreate(categoryName).id();
        }

        Long bindingId = masterDataRepository.bindEntityToCategory(entityType, entityId, categoryId);
        return entityType.getName() + "_category:" + bindingId;
    }
}
