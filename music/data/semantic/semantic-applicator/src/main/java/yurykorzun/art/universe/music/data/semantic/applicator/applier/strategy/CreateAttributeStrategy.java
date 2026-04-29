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
import yurykorzun.art.universe.music.data.semantic.model.ProposalType;

import java.time.LocalDate;

@Component
public class CreateAttributeStrategy implements ProposalApplyStrategy {

    private static final String PROPOSAL_TYPE = "CREATE_ATTRIBUTE";

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
            ProposalPayloads.requireString(payload, "entity_type", PROPOSAL_TYPE)
        );
        Long entityId = entityResolver.require(payload, "entity_id", "entity_ref", context, PROPOSAL_TYPE, "entity");
        String attributeCode = ProposalPayloads.requireString(payload, "attribute_code", PROPOSAL_TYPE);
        String rawValue = ProposalPayloads.requireString(payload, "value", PROPOSAL_TYPE);

        AttributeRepository.AttributeDef def = attributeRepository.findDefByCode(attributeCode);
        if (def == null) {
            throw new IllegalStateException(PROPOSAL_TYPE + ": attribute_def not found for code=" + attributeCode);
        }

        LocalDate eventDate = ProposalPayloads.readDate(payload, "event_date");
        LocalDate validFrom = ProposalPayloads.readDate(payload, "valid_from");
        LocalDate validTill = ProposalPayloads.readDate(payload, "valid_till");
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
