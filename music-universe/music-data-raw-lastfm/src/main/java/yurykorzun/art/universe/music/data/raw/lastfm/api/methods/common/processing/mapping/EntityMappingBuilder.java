package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.mapping;

import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.dto.EntityDto;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.mapping.attributes.EntityAttributeHandler;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.BaseLastfmEntity;
import yurykorzun.art.universe.music.data.raw.lastfm.common.UniquenessSupport;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class EntityMappingBuilder {

    public static <E extends BaseLastfmEntity, D extends EntityDto<E>> EntityMappingResult<E, D> buildMapping(
        List<D> dtos,
        List<E> existingEntities,
        LastfmApiCall sourceApiCall,
        EntityFactory<E, D> entityFactory,
        List<EntityAttributeHandler<E, ?, D>> attrHandlers
    ) {
        // create mappings using uniqueness keys as a connection
        HashMap<String, EntityMapping<E, D>> map = dtos.stream()
            .collect(Collectors.toMap(
                UniquenessSupport::getUniqueKey,
                EntityMapping::new,
                (existing, replacement) -> existing,
                HashMap::new
            ));
        EntityMappingResult<E, D> mappings = new EntityMappingResult<>(map, sourceApiCall);

        // update mappings with existing entities
        existingEntities.forEach(entity -> {
            EntityMapping<E, D> mapping = mappings.get(entity.getUniqueKey());
            if (mapping != null) {
                mapping.setOldEntity(entityFactory.clone(entity));
                mapping.setNewEntity(entity);
            }
        });

        // update mappings with new entities
        mappings.forEach((key, mapping) -> {
            if (mapping.getNewEntity() == null) {
                mapping.setNewEntity(entityFactory.fromDto(mapping.getDto(), sourceApiCall));
                mapping.setNew(true);
                mapping.setShouldBeSaved(true);
            }
        });

        // find updated entities and mark for saving
        mappings.forEach((key, mapping) -> {
            for (EntityAttributeHandler<E, ?, D> handler : attrHandlers) {
                if (handler.isAttributeEmbedded() &&
                    handler.hasAttributeChanged(mapping)) {
                    // copy new value from DTO
                    handler.copyTo(mapping.getNewEntity(), mapping.getDto());
                    mapping.setShouldBeSaved(true);
                }
            }
            mapping.setStage(EntityMappingStage.INITIALIZED);
        });
        return mappings;
    }
}
