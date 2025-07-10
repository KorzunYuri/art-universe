package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing;

import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.dto.EntityDto;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.mapping.EntityFactory;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.mapping.EntityMappingBuilder;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.mapping.EntityMappingResult;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.mapping.attributes.AttributeHistoryBuilder;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.mapping.attributes.EntityAttributeHandler;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.persistence.EntityPersister;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.entity.LastfmAttributeHistoryRecord;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.BaseLastfmEntity;

import java.util.List;
import java.util.function.Function;

public class LastfmApiDtoProcessor<E extends BaseLastfmEntity, D extends EntityDto<E>> {

    /**
     * Orchestrates the DTO processing. Return collections of updated objects wrapped into {@link LastfmApiDtoProcessingResult}:
     * <ul>
     *     <li>new/updated entities (with new ids)</li>
     *     <li>new attribute_history records</li>
     * </ul>
     * @param dtos              list of incoming DTOs, result of API response parsing
     * @param existingEntities  list of existing entities
     * @param sourceApiCall source api response (holds reference to source api call)
     * @param entityFactory     class capable of producing entities from DTOs
     * @param attrHandlers      handlers for all the attributes that have to be processed within this call
     * @param entitySaver       function responsible for persisting entities
     * @param attrSaver         function responsible for persisting {@link LastfmAttributeHistoryRecord}
     *
     * @return {@link LastfmApiDtoProcessingResult} containing:
     * <ul>
     *     <li><b>only saved</b> (new / updated) entities</li>
     *     <li><b>only saved</b> (new / updated) attribute values</li>
     * </ul>
     */
    public LastfmApiDtoProcessingResult<E, D> process(
        List<D> dtos,
        List<E> existingEntities,
        LastfmApiCall sourceApiCall,
        EntityFactory<E, D> entityFactory,
        List<EntityAttributeHandler<E, ?, D>> attrHandlers,
        Function<List<E>, List<E>> entitySaver,
        Function<List<LastfmAttributeHistoryRecord>, List<LastfmAttributeHistoryRecord>> attrSaver
    ) {
        // Build mapping
        EntityMappingResult<E, D> mappings = EntityMappingBuilder.buildMapping(dtos, existingEntities, sourceApiCall, entityFactory, attrHandlers);

        // Persist new/updated entities and update mapping with new IDs
        List<E> savedEntities = processEntities(mappings, entitySaver);

        // Build and persist attribute history records
        List<LastfmAttributeHistoryRecord> attrRecords = processAttributeRecords(mappings, attrHandlers, sourceApiCall, attrSaver);

        return new LastfmApiDtoProcessingResult<>(savedEntities, attrRecords, mappings);
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
        EntityMappingResult<E, D> mappings,
        Function<List<E>, List<E>> entitySaver
    ) {
        return EntityPersister.persistEntities(mappings, entitySaver);
    }

    /**
     * Creates and persists new attribute records
     *
     * @param mappings          object containing dto-to-entity bindings
     * @param attrHandlers      objects responsible for getting/setting attribute values to entity
     * @param sourceApiCall     source api call
     * @param attrSaver         saver method receiving a list of {@link LastfmAttributeHistoryRecord}
     *
     * @return  List of records returned by saver method
     */
    private List<LastfmAttributeHistoryRecord> processAttributeRecords(
        EntityMappingResult<E, D> mappings,
        List<EntityAttributeHandler<E, ?, D>> attrHandlers,
        LastfmApiCall sourceApiCall,
        Function<List<LastfmAttributeHistoryRecord>, List<LastfmAttributeHistoryRecord>> attrSaver
    ) {
        List<LastfmAttributeHistoryRecord> attrRecords = AttributeHistoryBuilder.buildAttributeHistoryRecords(
            mappings, attrHandlers, sourceApiCall);
        attrSaver.apply(attrRecords);
        return attrRecords;
    }
}
