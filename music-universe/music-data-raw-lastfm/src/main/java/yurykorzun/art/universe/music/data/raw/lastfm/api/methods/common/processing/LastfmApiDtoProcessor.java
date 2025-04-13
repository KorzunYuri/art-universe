package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing;

import jakarta.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiResponse;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.dto.EntityDto;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.mapping.EntityFactory;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.mapping.EntityMappingBuilder;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.mapping.EntityMappings;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.mapping.EntityRelationBuilder;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.mapping.attributes.AttributeHistoryBuilder;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.mapping.attributes.EntityAttributeHandler;
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
    @Nullable
    private final EntityRelationBuilder<E, D> relationBuilder;

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

    public LastfmApiDtoProcessor(
        EntityMappingBuilder<E, D> mappingBuilder,
        EntityPersister<E, D> entityPersister,
        AttributeHistoryBuilder<E, D> attributeHistoryBuilder
    ) {
        this(mappingBuilder, entityPersister, attributeHistoryBuilder, null);
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
     * @param sourceApiResponse source api response (holds reference to source api call)
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
        LastfmApiResponse sourceApiResponse,
        EntityFactory<E, D> entityFactory,
        List<EntityAttributeHandler<E, ?, D>> attrHandlers,
        Function<List<E>, List<E>> entitySaver,
        Function<List<LastfmAttributeHistoryRecord>, List<LastfmAttributeHistoryRecord>> attrSaver,
        @Nullable Consumer<List<LastfmEntityRelation>> relationSaver
    ) {
        // Build mapping
        EntityMappings<E, D> mappings = mappingBuilder.buildMapping(dtos, existingEntities, sourceApiResponse, entityFactory, attrHandlers);

        // Persist new/updated entities and update mapping with new IDs
        List<E> savedEntities = processEntities(mappings, entitySaver);

        // Build and persist attribute history records
        List<LastfmAttributeHistoryRecord> attrRecords = processAttributeRecords(mappings, attrHandlers, sourceApiResponse, attrSaver);

        // (optionally) build and persist relations
        List<LastfmEntityRelation> relations = processEntityRelations(mappings, sourceApiResponse, relationSaver);

        return new LastfmApiDtoProcessingResult<>(savedEntities, attrRecords, relations);
    }

    /**
     * Process a non-scoped dto list, which means it doesn't produce new entity relations.
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
        return processDtos(dtos, existingEntities, apiResponse, entityFactory, attrHandlers, entitySaver, attrSaver, null);
    }

    /**
     * Creates and updates entities, updates mappings respectively.
     *
     * @param mappings    object containing dto-to-entity bindings
     * @param entitySaver saver method receiving a list of entities
     *
     * @return  List of records returned by saver method
     */
    private List<E> processEntities(
        EntityMappings<E, D> mappings,
        Function<List<E>, List<E>> entitySaver
    ) {
        List<E> savedEntities = entityPersister.persistEntities(mappings, entitySaver);
        log.info("{}: created/updated {} entities", getClass().getSimpleName(), savedEntities.size());
        return savedEntities;
    }

    /**
     * Creates and persists new attribute records
     *
     * @param mappings          object containing dto-to-entity bindings
     * @param attrHandlers      objects responsible for getting/setting attribute values to entity
     * @param sourceApiResponse source api response containing reference to api call
     * @param attrSaver         saver method receiving a list of {@link LastfmAttributeHistoryRecord}
     *
     * @return  List of records returned by saver method
     */
    private List<LastfmAttributeHistoryRecord> processAttributeRecords(
        EntityMappings<E, D> mappings,
        List<EntityAttributeHandler<E, ?, D>> attrHandlers,
        LastfmApiResponse sourceApiResponse,
        Function<List<LastfmAttributeHistoryRecord>, List<LastfmAttributeHistoryRecord>> attrSaver
    ) {
        List<LastfmAttributeHistoryRecord> attrRecords = attributeHistoryBuilder.buildAttributeHistoryRecords(
            mappings, attrHandlers, sourceApiResponse.getApiCall());
        attrSaver.apply(attrRecords);
        log.info("{}: created {} attribute history records", getClass().getSimpleName(), attrRecords.size());
        return attrRecords;
    }

    /**
     * If both relation builder and relation saved were provided, builds new entity relations and pass them for saving.
     *
     * @param mappings          object containing dto-to-entity bindings
     * @param sourceApiResponse source api response containing reference to api call
     * @param relationSaver     saver method receiving a list of {@link LastfmEntityRelation}
     *
     * @return  List of records PASSED to the saver method
     */
    private List<LastfmEntityRelation> processEntityRelations(
        EntityMappings<E, D> mappings,
        LastfmApiResponse sourceApiResponse,
        @Nullable Consumer<List<LastfmEntityRelation>> relationSaver
    ) {
        List<LastfmEntityRelation> relations = Collections.emptyList();

        if (relationBuilder != null && relationSaver != null) {
            relations = relationBuilder.buildEntityRelations(mappings, sourceApiResponse.getApiCall());
            relationSaver.accept(relations);
            log.info("{}: created {} entity relations", getClass().getSimpleName(), relations.size());
        }
        return relations;
    }
}
