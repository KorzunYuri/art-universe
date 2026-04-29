package yurykorzun.art.universe.music.data.semantic.applicator.applier.strategy;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.common.domain.entity.MasterEntityType;
import yurykorzun.art.universe.music.data.semantic.applicator.applier.ApplicationContext;
import yurykorzun.art.universe.music.data.semantic.applicator.applier.ProposalApplyStrategy;
import yurykorzun.art.universe.music.data.semantic.applicator.applier.ProposalRow;
import yurykorzun.art.universe.music.data.semantic.applicator.applier.support.EntityReferenceResolver;
import yurykorzun.art.universe.music.data.semantic.applicator.applier.support.ProposalPayloads;
import yurykorzun.art.universe.music.data.semantic.applicator.repository.AttributeRepository;
import yurykorzun.art.universe.music.data.semantic.model.PayloadFields;
import yurykorzun.art.universe.music.data.semantic.model.ProposalType;

import java.time.LocalDate;

@Component
public class CreateAttributeStrategy implements ProposalApplyStrategy {

    private final AttributeRepository attributeRepository;
    private final EntityReferenceResolver entityResolver;

    public CreateAttributeStrategy(AttributeRepository attributeRepository, EntityReferenceResolver entityResolver) {
        this.attributeRepository = attributeRepository;
        this.entityResolver = entityResolver;
    }

    @Override
    public ProposalType supportedType() {
        return ProposalType.CREATE_ATTRIBUTE;
    }

    @Override
    public String apply(JsonNode payload, ProposalRow proposal, ApplicationContext context) {
        MasterEntityType entityType = MasterEntityType.fromString(
            ProposalPayloads.requireString(payload, PayloadFields.ENTITY_TYPE, proposalTypeName())
        );
        Long entityId = entityResolver.require(payload, PayloadFields.ENTITY_ID, PayloadFields.ENTITY_REF, context, proposalTypeName(), "entity");
        String attributeCode = ProposalPayloads.requireString(payload, PayloadFields.ATTRIBUTE_CODE, proposalTypeName());
        String rawValue = ProposalPayloads.requireString(payload, PayloadFields.VALUE, proposalTypeName());

        AttributeRepository.AttributeDef def = attributeRepository.findDefByCode(attributeCode);
        if (def == null) {
            throw new IllegalStateException(proposalTypeName() + ": attribute_def not found for code=" + attributeCode);
        }

        LocalDate eventDate = ProposalPayloads.readDate(payload, PayloadFields.EVENT_DATE);
        LocalDate validFrom = ProposalPayloads.readDate(payload, PayloadFields.VALID_FROM);
        LocalDate validTill = ProposalPayloads.readDate(payload, PayloadFields.VALID_TILL);
        String sourceRef = "proposal:" + proposal.getId();

        Long id = attributeRepository.createValue(
            entityType, entityId, def.id(), def.dataType(),
            rawValue, proposal.getConfidence(),
            eventDate, validFrom, validTill,
            sourceRef
        );
        return "entity_attribute_value:" + id;
    }
}
