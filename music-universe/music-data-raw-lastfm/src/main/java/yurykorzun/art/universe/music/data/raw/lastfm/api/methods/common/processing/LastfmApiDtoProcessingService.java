package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing;

import org.springframework.stereotype.Service;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiResponse;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.dto.EntityDto;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.mapping.EntityFactory;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.mapping.EntityMappingBuilder;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.mapping.EntityRelationBuilder;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.mapping.attributes.DefaultAttributeHistoryBuilder;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.mapping.attributes.EntityAttributeHandler;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.persistence.DefaultEntityPersister;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.service.LastfmAttributeHistoryService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.BaseLastfmEntity;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.service.LastfmEntityRelationService;

import java.util.List;
import java.util.function.Function;

/**
 * This class generifies and simplifies calls of {@link LastfmApiDtoProcessor}.
 */
@Service
public class LastfmApiDtoProcessingService {

    private final LastfmAttributeHistoryService attributeHistoryService;
    private final LastfmEntityRelationService relationService;

    public LastfmApiDtoProcessingService(
        LastfmEntityRelationService relationService,
        LastfmAttributeHistoryService attributeHistoryService
    ) {
        this.attributeHistoryService = attributeHistoryService;
        this.relationService = relationService;
    }

    public <E extends BaseLastfmEntity, D extends EntityDto> LastfmApiDtoProcessingResult<E> processDtosWithRelations(
        List<D> dtos,
        List<E> existingEntities,
        LastfmApiResponse sourceApiResponse,
        EntityFactory<E, D> entityFactory,
        List<EntityAttributeHandler<E, ?, D>> attrHandlers,
        Function<List<E>, List<E>> entitySaver
    ) {
        return processDtosUsingProcessor(dtos, existingEntities, sourceApiResponse, entityFactory, attrHandlers, entitySaver, true);
    }

    public <E extends BaseLastfmEntity, D extends EntityDto> LastfmApiDtoProcessingResult<E> processDtosWithoutRelations(
        List<D> dtos,
        List<E> existingEntities,
        LastfmApiResponse sourceApiResponse,
        EntityFactory<E, D> entityFactory,
        List<EntityAttributeHandler<E, ?, D>> attrHandlers,
        Function<List<E>, List<E>> entitySaver
    ) {
        return processDtosUsingProcessor(dtos, existingEntities, sourceApiResponse, entityFactory, attrHandlers, entitySaver, false);
    }

    private <E extends BaseLastfmEntity, D extends EntityDto> LastfmApiDtoProcessingResult<E> processDtosUsingProcessor(
        List<D> dtos,
        List<E> existingEntities,
        LastfmApiResponse sourceApiResponse,
        EntityFactory<E, D> entityFactory,
        List<EntityAttributeHandler<E, ?, D>> attrHandlers,
        Function<List<E>, List<E>> entitySaver,
        boolean shouldProcessEntityRelations
    ) {
        LastfmApiDtoProcessor<E, D> mappingService = new LastfmApiDtoProcessor<>(
            new EntityMappingBuilder<>(),
            new DefaultEntityPersister<>(),
            new DefaultAttributeHistoryBuilder<>(),
            new EntityRelationBuilder<>()
        );

        return mappingService.processDtos(
            dtos,
            existingEntities,
            sourceApiResponse,
            entityFactory,
            attrHandlers,
            entitySaver,
            attributeHistoryService::upsertCandidateValues,
            shouldProcessEntityRelations ? relationService::upsertEntityRelations : null
        );
    }

}
