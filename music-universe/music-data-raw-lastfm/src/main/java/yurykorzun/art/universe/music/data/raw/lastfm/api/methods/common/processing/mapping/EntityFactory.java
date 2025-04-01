package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.mapping;

import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiResponse;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.dto.EntityDto;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.BaseLastfmEntity;

/**
 * Helper class for creating new entity objects from DTOs or existing entities,
 * @param <E>   entity
 * @param <D>   DTO
 */
public abstract class EntityFactory<E extends BaseLastfmEntity, D extends EntityDto> {

    /**
     * Create entity from DTO and other info held in response object.
     * Resulting entity is supposed to be saved.
     */
    protected abstract E fromDto(D dto, LastfmApiResponse response);

    /**
     * Clone (no deep-clone) the entity, including id.
     */
    protected abstract E clone(E entity);
}
