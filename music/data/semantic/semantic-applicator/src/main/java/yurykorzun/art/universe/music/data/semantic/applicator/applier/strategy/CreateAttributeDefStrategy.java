package yurykorzun.art.universe.music.data.semantic.applicator.applier.strategy;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.common.domain.entity.MasterEntityType;
import yurykorzun.art.universe.music.data.semantic.applicator.applier.ApplicationContext;
import yurykorzun.art.universe.music.data.semantic.applicator.applier.ProposalApplyStrategy;
import yurykorzun.art.universe.music.data.semantic.applicator.applier.ProposalRow;
import yurykorzun.art.universe.music.data.semantic.applicator.applier.support.ProposalPayloads;
import yurykorzun.art.universe.music.data.semantic.applicator.repository.AttributeRepository;
import yurykorzun.art.universe.music.data.master.model.attribute.AttributeComputationType;
import yurykorzun.art.universe.music.data.master.model.attribute.AttributeDataType;
import yurykorzun.art.universe.music.data.master.model.attribute.AttributeTemporalType;
import yurykorzun.art.universe.music.data.semantic.model.PayloadFields;
import yurykorzun.art.universe.music.data.semantic.model.ProposalType;

import java.util.ArrayList;
import java.util.List;

@Component
public class CreateAttributeDefStrategy implements ProposalApplyStrategy {

    private final AttributeRepository attributeRepository;

    public CreateAttributeDefStrategy(AttributeRepository attributeRepository) {
        this.attributeRepository = attributeRepository;
    }

    @Override
    public ProposalType supportedType() {
        return ProposalType.CREATE_ATTRIBUTE_DEF;
    }

    @Override
    public String apply(JsonNode payload, ProposalRow proposal, ApplicationContext context) {
        String code = ProposalPayloads.requireString(payload, PayloadFields.CODE, proposalTypeName());
        String name = ProposalPayloads.requireString(payload, PayloadFields.NAME, proposalTypeName());
        AttributeDataType dataType = AttributeDataType.fromString(
            ProposalPayloads.requireString(payload, PayloadFields.DATA_TYPE, proposalTypeName())
        );
        int temporalType = payload.hasNonNull(PayloadFields.TEMPORAL_TYPE)
            ? AttributeTemporalType.fromString(payload.get(PayloadFields.TEMPORAL_TYPE).asText()).getCode()
            : attributeRepository.defaultTemporalType();

        AttributeRepository.AttributeDef existing = attributeRepository.findDefByCode(code);
        Long defId = existing != null ? existing.id() : attributeRepository.createDef(
            code, name, dataType, temporalType, AttributeComputationType.SEMANTIC.getCode(), false
        );

        List<MasterEntityType> applicable = readApplicable(payload);
        if (!applicable.isEmpty()) {
            attributeRepository.bindApplicability(defId, applicable);
        }
        context.registerSynthId(proposal.getSynthId(), defId);

        return "attribute_def:" + defId + (existing != null ? ":existing" : ":created");
    }

    private List<MasterEntityType> readApplicable(JsonNode payload) {
        JsonNode node = payload.get(PayloadFields.APPLICABLE_TO);
        List<MasterEntityType> result = new ArrayList<>();
        if (node != null && node.isArray()) {
            for (JsonNode item : node) {
                String raw = item.asText(null);
                if (raw != null && !raw.isBlank()) {
                    result.add(MasterEntityType.fromString(raw));
                }
            }
        }
        return result;
    }
}
