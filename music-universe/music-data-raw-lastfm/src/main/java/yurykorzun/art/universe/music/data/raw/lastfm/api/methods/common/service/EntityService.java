package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.service;

import org.springframework.transaction.annotation.Transactional;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.dto.EntityDto;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.BaseLastfmEntity;

import java.util.List;

/**
 * Universal interface for entity services with default method for DTO processing
 * @param <E> Entity type
 */
public interface EntityService<E extends BaseLastfmEntity> {

    /**
     * Find entities by their unique keys (names, URLs, etc.)
     * Each service implements this method calling their specific finder method
     */
    List<E> findEntitiesByUniqueKeys(List<String> uniqueKeys);

    /**
     * Save entities and return saved entities with IDs
     */
    @Transactional
    List<E> saveAll(List<E> entities);

    /**
     * Default method that extracts unique keys from DTOs and finds existing entities
     * This eliminates the need for specific DTO adapters
     */
    default <D extends EntityDto<E>> List<E> findExistingEntities(List<D> dtos) {
        List<String> uniqueKeys = dtos.stream()
            .map(EntityDto::getUniqueKey)
            .toList();
        return findEntitiesByUniqueKeys(uniqueKeys);
    }
}
