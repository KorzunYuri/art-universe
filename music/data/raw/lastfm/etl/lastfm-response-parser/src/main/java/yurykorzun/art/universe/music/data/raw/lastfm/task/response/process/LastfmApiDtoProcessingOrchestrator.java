package yurykorzun.art.universe.music.data.raw.lastfm.task.response.process;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.EntityDto;
import yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.mapping.EntityFactory;
import yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.mapping.EntityMapping;
import yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.mapping.EntityMappingResult;
import yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.mapping.EntityMappingStage;
import yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.mapping.attributes.AttributeHistoryBuilder;
import yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.mapping.attributes.EntityAttributeHandler;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.dto.LastfmAttributeHistoryCandidate;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.common.BaseLastfmEntity;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.service.attribute.LastfmAttributeHistoryStagingService;
import yurykorzun.art.universe.music.data.raw.lastfm.common.UniquenessSupport;
import yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.service.EntityService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class LastfmApiDtoProcessingOrchestrator {

    private final LastfmAttributeHistoryStagingService attributeHistoryService;

    public LastfmApiDtoProcessingOrchestrator(LastfmAttributeHistoryStagingService attributeHistoryService) {
        this.attributeHistoryService = attributeHistoryService;
    }

    /**
     * Orchestrates the DTO processing. Return collections of updated objects wrapped into {@link LastfmApiDtoProcessingResult}:
     * <ul>
     *     <li>new/updated entities (with new ids)</li>
     *     <li>new attribute_history records</li>
     * </ul>
     * @param sourceApiCall     source api call
     * @param dtos              list of incoming DTOs, result of API response parsing
     * @param entityFactory     class capable of producing entities from DTOs
     * @param attrHandlers      handlers for all the attributes that have to be processed within this call
     * @param entityService     service capable of mapping existing entities to dtos and persisting entities
     *
     * @return {@link LastfmApiDtoProcessingResult} containing:
     * <ul>
     *     <li><b>only saved</b> (new / updated) entities</li>
     *     <li><b>only saved</b> (new / updated) attribute values</li>
     * </ul>
     */
    public <E extends BaseLastfmEntity, D extends EntityDto<E>> LastfmApiDtoProcessingResult<E, D> process(
        LastfmApiCall sourceApiCall,
        List<D> dtos,
        EntityFactory<E, D> entityFactory,
        List<EntityAttributeHandler<E, ?, D>> attrHandlers,
        EntityService<E> entityService
    ) {
        // Get DTO -> existing entities mapping from service
        Map<D, E> dtoToEntityMap = entityService.mapDtoToExistingEntities(dtos);

        // Build complete mapping with found entities
        EntityMappingResult<E, D> mappings = buildMapping(
            dtos, 
            dtoToEntityMap, 
            sourceApiCall, 
            entityFactory, 
            attrHandlers
        );

        // Save new/updated entities
        List<E> savedEntities = saveChangedEntities(mappings, entityService::saveAll);
        updateMappingsWithSavedEntities(mappings, savedEntities);

        // Get actual entities
        List<E> actualEntities = mappings.values().stream()
            .map(this::getActualEntity)
            .collect(Collectors.toList());

        // Create and save attribute history records
        int attrRecordsCount = processAttributeRecords(
            mappings, attrHandlers, sourceApiCall
        );

        return new LastfmApiDtoProcessingResult<>(actualEntities, attrRecordsCount, mappings);
    }

    /**
     * Builds DTO -> EntityMapping with already found entities
     */
    private <E extends BaseLastfmEntity, D extends EntityDto<E>> EntityMappingResult<E, D> buildMapping(
        List<D> dtos,
        Map<D, E> dtoToEntityMap,
        LastfmApiCall sourceApiCall,
        EntityFactory<E, D> entityFactory,
        List<EntityAttributeHandler<E, ?, D>> attrHandlers
    ) {
        Map<D, EntityMapping<E, D>> mappings = new HashMap<>();

        for (D dto : dtos) {
            EntityMapping<E, D> mapping = new EntityMapping<>(dto);
            E existingEntity = dtoToEntityMap.get(dto);

            if (existingEntity != null) {
                // Entity found - clone for change tracking
                mapping.setOldEntity(entityFactory.clone(existingEntity));
                mapping.setNewEntity(existingEntity);
            } else {
                // Entity not found - create new one
                mapping.setNewEntity(entityFactory.fromDto(dto, sourceApiCall));
                mapping.setNew(true);
                mapping.setShouldBeSaved(true);
            }

            mappings.put(dto, mapping);
        }

        EntityMappingResult<E, D> result = new EntityMappingResult<>(mappings, sourceApiCall);
        
        // Check attribute changes and mark for saving
        markChangedEntitiesForSaving(result, attrHandlers);
        
        return result;
    }

    /**
     * Checks attribute changes and marks entities for saving
     */
    private <E extends BaseLastfmEntity, D extends EntityDto<E>> void markChangedEntitiesForSaving(
        EntityMappingResult<E, D> mappings,
        List<EntityAttributeHandler<E, ?, D>> attrHandlers
    ) {
        mappings.forEach((dto, mapping) -> {
            if (!mapping.isNew()) {
                for (EntityAttributeHandler<E, ?, D> handler : attrHandlers) {
                    if (handler.isAttributeEmbedded() && handler.hasAttributeChanged(mapping)) {
                        // Copy new value from DTO
                        handler.copyTo(mapping.getNewEntity(), mapping.getDto());
                        mapping.setShouldBeSaved(true);
                    }
                }
            }
            mapping.setStage(EntityMappingStage.INITIALIZED);
        });
    }

    /**
     * Saves changed entities
     */
    private <E extends BaseLastfmEntity, D extends EntityDto<E>> List<E> saveChangedEntities(
        EntityMappingResult<E, D> mappings,
        Function<List<E>, List<E>> entitySaver
    ) {
        List<E> entitiesToSave = mappings.values().stream()
            .filter(EntityMapping::isShouldBeSaved)
            .map(EntityMapping::getNewEntity)
            .collect(Collectors.toList());
        
        return entitySaver.apply(entitiesToSave);
    }

    /**
     * Updates mappings with saved entities (with new IDs)
     */
    private <E extends BaseLastfmEntity, D extends EntityDto<E>> void updateMappingsWithSavedEntities(
        EntityMappingResult<E, D> mappings, 
        List<E> savedEntities
    ) {
        // Create map of saved entities by their content for matching
        Map<String, E> savedEntitiesMap = savedEntities.stream()
            .collect(Collectors.toMap(
                UniquenessSupport::getUniqueKey,
                entity -> entity,
                (existing, replacement) -> replacement
            ));

        mappings.forEach((dto, mapping) -> {
            if (mapping.isShouldBeSaved()) {
                String contentKey = mapping.getNewEntity().getUniqueKey();
                E savedEntity = savedEntitiesMap.get(contentKey);
                if (savedEntity != null) {
                    mapping.setNewEntity(savedEntity);
                    mapping.setStage(EntityMappingStage.ENTITY_SAVED);
                }
            }
        });
    }

    /**
     * Gets actual entity from mapping
     */
    private <E extends BaseLastfmEntity, D extends EntityDto<E>> E getActualEntity(EntityMapping<E, D> mapping) {
        return mapping.getNewEntity() != null ? mapping.getNewEntity() : mapping.getOldEntity();
    }

    /**
     * Creates and saves attribute history records
     */
    private <E extends BaseLastfmEntity, D extends EntityDto<E>> int processAttributeRecords(
        EntityMappingResult<E, D> mappings,
        List<EntityAttributeHandler<E, ?, D>> attrHandlers,
        LastfmApiCall sourceApiCall
    ) {
        List<LastfmAttributeHistoryCandidate> attrRecords = AttributeHistoryBuilder.buildAttributeHistoryRecords(
            mappings, attrHandlers, sourceApiCall
        );
        attributeHistoryService.upsertCandidateValues(attrRecords);
        return attrRecords.size();
    }
}
