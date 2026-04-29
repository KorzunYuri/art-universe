package yurykorzun.art.universe.music.data.semantic.applicator.applier.strategy;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.common.domain.entity.MasterEntityType;
import yurykorzun.art.universe.music.data.master.model.DataSource;
import yurykorzun.art.universe.music.data.semantic.applicator.applier.ApplicationContext;
import yurykorzun.art.universe.music.data.semantic.applicator.applier.ProposalApplyStrategy;
import yurykorzun.art.universe.music.data.semantic.applicator.applier.ProposalRow;
import yurykorzun.art.universe.music.data.semantic.applicator.applier.support.EntityReferenceResolver;
import yurykorzun.art.universe.music.data.semantic.applicator.applier.support.ProposalPayloads;
import yurykorzun.art.universe.music.data.semantic.applicator.repository.ExternalBindingRepository;
import yurykorzun.art.universe.music.data.semantic.model.PayloadFields;
import yurykorzun.art.universe.music.data.semantic.model.ProposalType;

@Component
public class BindExternalEntityStrategy implements ProposalApplyStrategy {

    private final ExternalBindingRepository externalBindingRepository;
    private final EntityReferenceResolver entityResolver;

    public BindExternalEntityStrategy(
        ExternalBindingRepository externalBindingRepository,
        EntityReferenceResolver entityResolver
    ) {
        this.externalBindingRepository = externalBindingRepository;
        this.entityResolver = entityResolver;
    }

    @Override
    public ProposalType supportedType() {
        return ProposalType.BIND_EXTERNAL_ENTITY;
    }

    @Override
    public String apply(JsonNode payload, ProposalRow proposal, ApplicationContext context) {
        DataSource dataSource = DataSource.fromString(
            ProposalPayloads.requireString(payload, PayloadFields.DATA_SOURCE, proposalTypeName())
        );
        long externalId = payload.path(PayloadFields.EXTERNAL_ID).asLong(-1);
        if (externalId < 0) {
            throw new IllegalArgumentException(proposalTypeName() + " requires " + PayloadFields.EXTERNAL_ID);
        }
        MasterEntityType entityType = MasterEntityType.fromString(
            ProposalPayloads.requireString(payload, PayloadFields.MASTER_ENTITY_TYPE, proposalTypeName())
        );
        Long masterId = entityResolver.require(
            payload, PayloadFields.MASTER_ENTITY_ID, PayloadFields.MASTER_ENTITY_REF, context, proposalTypeName(), "master"
        );

        Long id = externalBindingRepository.createBinding(entityType, masterId, dataSource.getCode(), externalId);
        return entityType.getName() + "_binding:" + id;
    }
}
