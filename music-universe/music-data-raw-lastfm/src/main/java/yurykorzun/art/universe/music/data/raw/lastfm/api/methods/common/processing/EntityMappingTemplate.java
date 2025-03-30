package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing;

import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiResponse;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.dto.EntityDto;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.entity.LastfmAttributeHistoryRecord;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.BaseLastfmEntity;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.LastfmEntityRelation;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * A helper class representing common flow of entity DTOs processing.
 * Given list of parsed DTOs and list of existing entities, the class is capable of determining:
 * <ul>
 *     <li>which entity is to be saved</li>
 *     <li>which entity is new</li>
 *     <li>which attribute values have changed and hence are to be saved</li>
 * </ul>
 * @param <E>   entity
 * @param <D>   DTO
 */
public class EntityMappingTemplate<E extends BaseLastfmEntity, D extends EntityDto> {

    private final HashMap<String, EntityMapping<E, D>> mappings;
    private final List<EntityAttributeHandler<E, ?, D>> attrHandlers;
    private final LastfmApiResponse sourceApiResponse;

    private EntityMappingStage stage;

    private List<LastfmEntityRelation> entityRelationsCache;

    /**
     * <p>There is a lot going on in the constructor,
     * because there is no need in 'inconsistent' state ('stage') and additional methods that change it.</p>
     * What is essential to know is that at the end of the constructor the class will hold a collection of 'mappings',
     * where every mapping corresponds to provided DTO and holds:
     * <ul>
     *     <li>source api call</li>
     *     <li>dto</li>
     *     <li>old version of entity, if it existed. <b>IMPORTANT! Used only for comparison!</b></li>
     *     <li>new or initial version of entity</li>
     * </ul>
     * Watch {@link EntityMapping} comments for additional info.
     * </p>
     */
    public EntityMappingTemplate(
        List<D> dtoList,
        List<E> existingEntities,
        LastfmApiResponse apiResponse,
        EntityFactory<E, D> entityFactory,
        List<EntityAttributeHandler<E, ?, D>> attrHandlers
    ) {
        this.stage = EntityMappingStage.NOT_INITIALIZED;
        this.sourceApiResponse = apiResponse;
        this.attrHandlers = attrHandlers;

        //  initialize mapping without entities
        mappings = dtoList.stream()
            .collect(Collectors.toMap(
                D::getName,
                EntityMapping::new,
                (existing, replacement) -> existing,
                HashMap::new
            ));

        //  add existing entities as old versions & copy them as new versions (that will be updated if needed)
        existingEntities.forEach(entity -> {
            EntityMapping<E, D> mapping = mappings.get(entity.getName());
            mapping.setOldEntity(entityFactory.clone(entity));
            mapping.setNewEntity(entity);
        });

        //  add new versions
        mappings.forEach((n, mapping) -> {
            if (mapping.getNewEntity() == null) {
                mapping.setNewEntity(entityFactory.fromDto(mapping.getDto(), apiResponse));
                mapping.setNew(true);
            }
        });

        // update entities with changed attributes
        final EntityMappingStage finalInitStage = EntityMappingStage.INITIALIZED;
        mappings.forEach((entityName, mapping) -> {
            attrHandlers.forEach(handler -> {
                if (handler.isAttributeEmbedded() && hasAttributeChanged(mapping, handler)) {
                    handler.copyTo(mapping.getNewEntity(), mapping.getDto());
                    mapping.setShouldBeSaved(true);
                }
            });
            mapping.setStage(finalInitStage);
        });
        this.stage = finalInitStage;
    }

    private boolean hasAttributeChanged(EntityMapping<E, D> mapping, EntityAttributeHandler<E, ?, D> extractor) {
        return mapping.getOldEntity() == null
            || !Objects.equals(extractor.extractFrom(mapping.getOldEntity()), extractor.extractFrom(mapping.getNewEntity()));
    }

    public List<E> saveUpdatedEntities(Function<List<E>, List<E>> saver) {

        if (!stage.isAtLeast(EntityMappingStage.INITIALIZED)) {
            throw new IllegalStateException("Mapping template is not INITIALIZED");
        }

        List<E> updatedEntities = saver.apply(getEntitiesToSave());

        //  update entities in mappings (new IDs is what we are interested in)
        stage = EntityMappingStage.ENTITY_SAVED;
        updatedEntities.forEach(entity -> {
            EntityMapping<E, D> mapping = mappings.get(entity.getName());
            mapping.setNewEntity(entity);
            mapping.setStage(stage);
        });

        return updatedEntities;
    }

    public List<E> getAllEntities() {
        if (!stage.isAtLeast(EntityMappingStage.ENTITY_SAVED)) {
            throw new IllegalStateException("New entities should be saved before we can rely on them");
        }

        return mappings.values().stream().map(EntityMapping::getNewEntity).toList();
    }

    public List<E> getEntitiesToSave() {
        return mappings.values().stream()
                .filter(EntityMapping::isShouldBeSaved)
                .map(EntityMapping::getNewEntity)
            .toList();
    }

    public List<LastfmAttributeHistoryRecord> saveUpdatedAttrValues(
        Function<List<LastfmAttributeHistoryRecord>, List<LastfmAttributeHistoryRecord>> saver
    ) {
        if (!stage.isAtLeast(EntityMappingStage.ENTITY_SAVED)) {
            throw new IllegalStateException("New entities should be saved before we can extract attributes");
        }

        return saver.apply(getAttrValuesToSave());
    }

    public List<LastfmAttributeHistoryRecord> getAttrValuesToSave() {
        return mappings.values().stream()
            .flatMap(m -> attrHandlers.stream()
                    .filter(h -> hasAttributeChanged(m, h) || h.shouldCreateNewValueUnconditionally())
                    .map(h -> this.createAttrValue(m, h)))
            .toList();
    }

    private LastfmAttributeHistoryRecord createAttrValue(EntityMapping<E, D> mapping, EntityAttributeHandler<E, ?, D> attrHandler) {

        LastfmApiCall sourceApiCall = sourceApiResponse.getApiCall();
        E entity = mapping.getNewEntity();

        LastfmAttributeHistoryRecord.LastfmAttributeHistoryRecordBuilder builder = LastfmAttributeHistoryRecord.builder()
                .attribute(attrHandler.getAttribute())
                .apiCallId(sourceApiCall.getId())
                .entityType(entity.getType())
                .entityId(entity.getId());

        if (attrHandler.isAttributeScoped()) {
            builder
                .scopeEntityType(sourceApiCall.getEntityType())
                .scopeEntityId(sourceApiCall.getEntityId());
        }

        switch (attrHandler.getAttribute().getDataType()) {
            case STRING: builder.stringValue((String)    attrHandler.extractFrom(entity));  break;
            case INTEGER: builder.intValue(   (Integer)   attrHandler.extractFrom(entity)); break;
        }

        return builder.build();
    }

    public void saveEntityRelations(Consumer<List<LastfmEntityRelation>> saver) {

        if (!stage.isAtLeast(EntityMappingStage.ENTITY_SAVED)) {
            throw new IllegalStateException(
                "New entities should be saved before we can build entity relations");
        }

        LastfmApiCall sourceApiCall = sourceApiResponse.getApiCall();
        if (sourceApiCall.getEntityId() == 0) {
            throw new IllegalArgumentException(
                String.format("Entity relations cannot be generated for api call %s of type %s as it doesn't have scoped entity",
                    sourceApiCall.getId(), sourceApiCall.getType()));
        }

        saver.accept(getEntityRelations());
        // TODO make service return actually inserted records?
    }

    public List<LastfmEntityRelation> getEntityRelations() {
        if (entityRelationsCache == null) {
            entityRelationsCache = mappings.values().stream()
                .map(m -> generateEntityRelation(m.getNewEntity()))
                .toList();
        }
        return entityRelationsCache;
    }

    public LastfmEntityRelation generateEntityRelation(E entity) {
        return LastfmEntityRelation.builder()
                .apiCall(sourceApiResponse.getApiCall())
                .scopeEntityType(sourceApiResponse.getApiCall().getEntityType())
                .scopeEntityId(sourceApiResponse.getApiCall().getEntityId())
                .entityType(entity.getType())
                .entityId(entity.getId())
            .build();
    }
}
