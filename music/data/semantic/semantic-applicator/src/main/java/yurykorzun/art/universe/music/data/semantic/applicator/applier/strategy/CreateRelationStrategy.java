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
import yurykorzun.art.universe.music.data.semantic.model.PayloadFields;
import yurykorzun.art.universe.music.data.semantic.model.ProposalType;

@Component
public class CreateRelationStrategy implements ProposalApplyStrategy {

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
            ProposalPayloads.requireString(payload, PayloadFields.SOURCE_ENTITY_TYPE, proposalTypeName())
        );
        MasterEntityType targetType = MasterEntityType.fromString(
            ProposalPayloads.requireString(payload, PayloadFields.TARGET_ENTITY_TYPE, proposalTypeName())
        );
        Long sourceId = entityResolver.require(
            payload, PayloadFields.SOURCE_ENTITY_ID, PayloadFields.SOURCE_ENTITY_REF, context, proposalTypeName(), "source"
        );
        Long targetId = entityResolver.require(
            payload, PayloadFields.TARGET_ENTITY_ID, PayloadFields.TARGET_ENTITY_REF, context, proposalTypeName(), "target"
        );
        Long relationTypeId = ProposalPayloads.requireLong(payload, PayloadFields.RELATION_TYPE_ID, proposalTypeName());

        RelationRepository.RelationType relationType = relationRepository.findRelationType(relationTypeId);
        if (relationType == null) {
            throw new IllegalArgumentException(proposalTypeName() + ": unknown relation_type_id=" + relationTypeId);
        }
        if (relationType.isSystem()) {
            throw new IllegalArgumentException(
                proposalTypeName() + ": relation_type_id=" + relationTypeId + " is a system type managed automatically by the platform"
            );
        }

        RelationRepository.CreatedRelation created = relationRepository.findOrCreateRelation(
            sourceType, sourceId, targetType, targetId, relationTypeId
        );
        return created.table() + ":" + created.id() + (created.existed() ? ":existing" : ":created");
    }
}
