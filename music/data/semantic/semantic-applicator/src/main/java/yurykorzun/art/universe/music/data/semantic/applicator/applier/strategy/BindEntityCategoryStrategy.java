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
public class BindEntityCategoryStrategy implements ProposalApplyStrategy {

    private final MasterDataRepository masterDataRepository;

    public BindEntityCategoryStrategy(MasterDataRepository masterDataRepository) {
        this.masterDataRepository = masterDataRepository;
    }

    @Override
    public ProposalType supportedType() {
        return ProposalType.BIND_ENTITY_CATEGORY;
    }

    @Override
    public String apply(JsonNode payload, ProposalRow proposal, ApplicationContext context) {
        String entityTypeRaw = payload.path("entity_type").asText();
        MasterEntityType entityType = MasterEntityType.fromString(entityTypeRaw);
        Long entityId = resolveEntityId(payload, context);

        Long categoryId = resolveCategoryId(payload, context);
        if (categoryId == null) {
            String categoryName = payload.path("category_name").asText();
            if (categoryName.isBlank()) {
                throw new IllegalArgumentException("BIND_ENTITY_CATEGORY requires category_id, category_ref, or category_name");
            }
            Long existing = masterDataRepository.findCategoryByName(categoryName);
            categoryId = existing != null ? existing : masterDataRepository.createCategory(categoryName);
        }

        Long bindingId = masterDataRepository.bindEntityToCategory(entityType, entityId, categoryId);
        return entityType.getName() + "_category:" + bindingId;
    }

    private Long resolveEntityId(JsonNode payload, ApplicationContext context) {
        if (payload.hasNonNull("entity_id")) {
            return payload.get("entity_id").asLong();
        }
        String ref = payload.path("entity_ref").asText(null);
        if (ref != null) {
            Long resolved = context.resolveSynthId(ref);
            if (resolved != null) {
                return resolved;
            }
        }
        throw new IllegalArgumentException("BIND_ENTITY_CATEGORY requires entity_id or resolvable entity_ref");
    }

    private Long resolveCategoryId(JsonNode payload, ApplicationContext context) {
        if (payload.hasNonNull("category_id")) {
            return payload.get("category_id").asLong();
        }
        String ref = payload.path("category_ref").asText(null);
        if (ref != null) {
            return context.resolveSynthId(ref);
        }
        return null;
    }
}
