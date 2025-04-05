package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing;

import lombok.extern.slf4j.Slf4j;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiResponse;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.dto.EntityDto;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.mapping.*;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.mapping.attributes.EntityAttributeHandler;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.mapping.attributes.AttributeHistoryBuilder;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.persistence.EntityPersister;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.entity.LastfmAttributeHistoryRecord;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.BaseLastfmEntity;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.LastfmEntityRelation;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

@Slf4j
public class LastfmApiDtoProcessor<E extends BaseLastfmEntity, D extends EntityDto> {

    private final EntityMappingBuilder<E, D> mappingBuilder;
    private final EntityPersister<E, D> entityPersister;
    private final AttributeHistoryBuilder<E, D> attributeHistoryBuilder;
    private final EntityRelationBuilder<E, D> relationBuilder;

    private EntityMappings<E, D> mappings;

    public LastfmApiDtoProcessor(
        EntityMappingBuilder<E, D> mappingBuilder,
        EntityPersister<E, D> entityPersister,
        AttributeHistoryBuilder<E, D> attributeHistoryBuilder,
        EntityRelationBuilder<E, D> relationBuilder
    ) {
        this.mappingBuilder = mappingBuilder;
        this.entityPersister = entityPersister;
        this.attributeHistoryBuilder = attributeHistoryBuilder;
        this.relationBuilder = relationBuilder;
    }

    /**
     * Orchestrates the DTO processing. Return collections of updated objects wrapped into {@link LastfmApiDtoProcessingResult}:
     * <ul>
     *     <li>new/updated entities (with new ids)</li>
     *     <li>new attribute_history records</li>
     *     <li>list of generated entity_relations <b>WITHOUT IDS</b></li>
     * </ul>
     * @param dtos              list of incoming DTOs, result of API response parsing
     * @param existingEntities  list of existing entities
     * @param apiResponse       source api response (holds reference to source api call)
     * @param entityFactory     class capable of producing entities from DTOs
     * @param attrHandlers      handlers for all the attributes that have to be processed within this call
     * @param entitySaver       function responsible for persisting entities
     * @param attrSaver         function responsible for persisting {@link LastfmAttributeHistoryRecord}
     * @param relationSaver     function responsible for persisting {@link LastfmEntityRelation}. DOESN'T RETURN VALUE.
     *
     * @return {@link LastfmApiDtoProcessingResult} containing:
     * <ul>
     *     <li><b>only saved</b> (new / updated) entities</li>
     *     <li><b>only saved</b> (new / updated) attribute values</li>
     *     <li><b>ALL</b> created entity_relations</li>
     * </ul>
     */
    public LastfmApiDtoProcessingResult<E> processDtos(
        List<D> dtos,
        List<E> existingEntities,
        LastfmApiResponse apiResponse,
        EntityFactory<E, D> entityFactory,
        List<EntityAttributeHandler<E, ?, D>> attrHandlers,
        Function<List<E>, List<E>> entitySaver,
        Function<List<LastfmAttributeHistoryRecord>, List<LastfmAttributeHistoryRecord>> attrSaver,
        Consumer<List<LastfmEntityRelation>> relationSaver
    ) {
        LastfmApiDtoProcessingResult<E> intermediateResult = processDtos(
            dtos, existingEntities, apiResponse, entityFactory, attrHandlers, entitySaver, attrSaver);

        // Build entity relations
        List<LastfmEntityRelation> relations = relationBuilder.buildEntityRelations(this.mappings, apiResponse.getApiCall());
        relationSaver.accept(relations);
        log.info("{}: created {} entity relations", getClass().getSimpleName(), relations.size());

        return new LastfmApiDtoProcessingResult<>(
            intermediateResult.savedEntities(),
            intermediateResult.createdAttributeValues(),
            relations);
    }

    /**
     * Process a non-scoped api call result, which means there will be no entity relations
     */
    public LastfmApiDtoProcessingResult<E> processDtos(
        List<D> dtos,
        List<E> existingEntities,
        LastfmApiResponse apiResponse,
        EntityFactory<E, D> entityFactory,
        List<EntityAttributeHandler<E, ?, D>> attrHandlers,
        Function<List<E>, List<E>> entitySaver,
        Function<List<LastfmAttributeHistoryRecord>, List<LastfmAttributeHistoryRecord>> attrSaver
    ) {
        // Build mapping
        this.mappings = mappingBuilder.buildMapping(
            dtos, existingEntities, apiResponse, entityFactory, attrHandlers);

        // Persist new/updated entities and update mapping with new IDs
        List<E> savedEntities = entityPersister.persistEntities(mappings, entitySaver);
        log.info("{}: created/updated {} entities", getClass().getSimpleName(), savedEntities.size());

        // Build and persist attribute history records
        List<LastfmAttributeHistoryRecord> attrRecords = attributeHistoryBuilder.buildAttributeHistoryRecords(
            mappings, attrHandlers, apiResponse.getApiCall());
        attrSaver.apply(attrRecords);
        log.info("{}: created {} attribute history records", getClass().getSimpleName(), attrRecords.size());

        return new LastfmApiDtoProcessingResult<>(savedEntities, attrRecords, Collections.emptyList());
    }
}
