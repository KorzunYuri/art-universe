package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.persistence;

import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.dto.EntityDto;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.mapping.EntityMappings;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.BaseLastfmEntity;

import java.util.List;
import java.util.function.Function;

public interface EntityPersister<E extends BaseLastfmEntity, D extends EntityDto> {

    /**
     * Persists new/updated entities and updates mappings with new IDs.
     */
    List<E> persistEntities(EntityMappings<E, D> mappings, Function<List<E>, List<E>> saver);

}