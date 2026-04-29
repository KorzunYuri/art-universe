package yurykorzun.art.universe.music.data.semantic.applicator.applier.strategy;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.common.domain.entity.MasterEntityType;
import yurykorzun.art.universe.music.data.semantic.applicator.applier.ApplicationContext;
import yurykorzun.art.universe.music.data.semantic.applicator.applier.ProposalApplyStrategy;
import yurykorzun.art.universe.music.data.semantic.applicator.applier.ProposalRow;
import yurykorzun.art.universe.music.data.semantic.applicator.applier.support.EntityReferenceResolver;
import yurykorzun.art.universe.music.data.semantic.applicator.applier.support.ProposalPayloads;
import yurykorzun.art.universe.music.data.semantic.applicator.repository.RelationRepository;
import yurykorzun.art.universe.music.data.semantic.model.ProposalType;

@Component
public class CreateRelationStrategy implements ProposalApplyStrategy {

    private static final String PROPOSAL_TYPE = "CREATE_RELATION";

    private final RelationRepository relationRepository;
    private final EntityReferenceResolver entityResolver;

    public CreateRelationStrategy(RelationRepository relationRepository, EntityReferenceResolver entityResolver) {
        this.relationRepository = relationRepository;
        this.entityResolver = entityResolver;
    }

    @Override
    public ProposalType supportedType() {
        return ProposalType.CREATE_RELATION;
    }

    @Override
    public String apply(JsonNode payload, ProposalRow proposal, ApplicationContext context) {
        MasterEntityType sourceType = MasterEntityType.fromString(
            ProposalPayloads.requireString(payload, "source_entity_type", PROPOSAL_TYPE)
        );
        MasterEntityType targetType = MasterEntityType.fromString(
            ProposalPayloads.requireString(payload, "target_entity_type", PROPOSAL_TYPE)
        );
        Long sourceId = entityResolver.require(
            payload, "source_entity_id", "source_entity_ref", context, PROPOSAL_TYPE, "source"
        );
        Long targetId = entityResolver.require(
            payload, "target_entity_id", "target_entity_ref", context, PROPOSAL_TYPE, "target"
        );
        Long relationTypeId = ProposalPayloads.requireLong(payload, "relation_type_id", PROPOSAL_TYPE);

        RelationRepository.RelationType relationType = relationRepository.findRelationType(relationTypeId);
        if (relationType == null) {
            throw new IllegalArgumentException(PROPOSAL_TYPE + ": unknown relation_type_id=" + relationTypeId);
        }
        if (relationType.isSystem()) {
            throw new IllegalArgumentException(
                PROPOSAL_TYPE + ": relation_type_id=" + relationTypeId + " is a system type managed automatically by the platform"
            );
        }

        RelationRepository.CreatedRelation created = relationRepository.findOrCreateRelation(
            sourceType, sourceId, targetType, targetId, relationTypeId
        );
        return created.table() + ":" + created.id() + (created.existed() ? ":existing" : ":created");
    }
}
