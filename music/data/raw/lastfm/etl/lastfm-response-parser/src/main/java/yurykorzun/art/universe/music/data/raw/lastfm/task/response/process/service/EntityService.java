package yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.service;

import org.springframework.transaction.annotation.Transactional;
import yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.EntityDto;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.common.BaseLastfmEntity;

import java.util.List;
import java.util.Map;

/**
 * Universal interface for entity services with default method for DTO processing
 * @param <E> Entity type
 */
public interface EntityService<E extends BaseLastfmEntity> {

    /**
     * Save entities and return saved entities with IDs
     */
    @Transactional
    List<E> saveAll(List<E> entities);

    <D extends EntityDto<E>> Map<D, E> mapDtoToExistingEntities(List<D> dtos);
}
