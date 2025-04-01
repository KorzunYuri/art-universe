package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.persistence;

import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.dto.EntityDto;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.mapping.EntityMapping;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.mapping.EntityMappingStage;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.mapping.EntityMappings;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.BaseLastfmEntity;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DefaultEntityPersister<E extends BaseLastfmEntity, D extends EntityDto> implements EntityPersister<E, D> {

    @Override
    public List<E> persistEntities(EntityMappings<E, D> mappings, Function<List<E>, List<E>> saver) {

        List<E> entitiesToSave = mappings.values().stream()
            .filter(EntityMapping::isShouldBeSaved)
            .map(EntityMapping::getNewEntity)
            .collect(Collectors.toList());
        List<E> saved = saver.apply(entitiesToSave);

        // update entities with ids we've just received
        saved.forEach(entity -> {
            EntityMapping<E, D> mapping = mappings.get(entity.getUniqueKey());
            if (mapping != null) {
                mapping.setNewEntity(entity);
                mapping.setStage(EntityMappingStage.ENTITY_SAVED);
            }
        });
        return saved;
    }
}
