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
import yurykorzun.art.universe.music.data.semantic.model.PayloadFields;
import yurykorzun.art.universe.music.data.semantic.model.ProposalType;

@Component
public class BindEntityCategoryStrategy implements ProposalApplyStrategy {

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
            ProposalPayloads.requireString(payload, PayloadFields.ENTITY_TYPE, proposalTypeName())
        );
        Long entityId = entityResolver.require(payload, PayloadFields.ENTITY_ID, PayloadFields.ENTITY_REF, context, proposalTypeName(), "entity");

        Long categoryId = entityResolver.resolveOrNull(
            payload, PayloadFields.CATEGORY_ID, PayloadFields.CATEGORY_REF, context, proposalTypeName(), "category"
        );
        if (categoryId == null) {
            String categoryName = ProposalPayloads.optionalString(payload, PayloadFields.CATEGORY_NAME);
            if (categoryName == null) {
                throw new IllegalArgumentException(
                    proposalTypeName() + " requires category_id, category_ref, or category_name"
                );
            }
            categoryId = categoryService.findOrCreate(categoryName).id();
        }

        Long bindingId = masterDataRepository.bindEntityToCategory(entityType, entityId, categoryId);
        return entityType.getName() + "_category:" + bindingId;
    }
}
