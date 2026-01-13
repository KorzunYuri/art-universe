package yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.mapping;

import yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.EntityDto;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.common.BaseLastfmEntity;
import yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.LastfmApiDtoProcessingOrchestrator;

/**
 * <p>Helper class for creating new entity objects from DTOs or existing entities.
 * Is used by {@link LastfmApiDtoProcessingOrchestrator}.</p>
 * <p>As long as DTOs are not 'stable' and usually have more than one 'variation' with different fieldsets,
 * it is suggested that we have a 'base' factory initializing fields that are present in all variations,
 * while leaving space for extending classes to initialize their specific fields during {@link #fromDto(EntityDto, LastfmApiCall)}.
 * </p>
 * @param <E>   entity
 * @param <D>   DTO
 */
public interface EntityFactory<E extends BaseLastfmEntity, D extends EntityDto<E>> {

    /**
     * Create entity from DTO and other info held in response object.
     * Resulting entity is supposed to be saved.
     */
    E fromDto(D dto, LastfmApiCall sourceApiCall);

    /**
     * Clone (no deep-clone) the entity, including id.
     */
    E clone(E entity);
}
