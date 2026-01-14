package yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.mapping;

import lombok.Getter;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.EntityDto;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.common.BaseLastfmEntity;

import java.util.Collection;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * Map of {@link EntityMapping} with DTOs as keys. Translates some basic {@link Map} methods and holds info about source apiCall.
 * @param <E>   entity
 * @param <D>   DTO
 */
public class EntityMappingResult<E extends BaseLastfmEntity, D extends EntityDto<E>> {
    
    private final Map<D, EntityMapping<E, D>> mappings;

    @Getter
    private final LastfmApiCall sourceApiCall;

    public EntityMappingResult(Map<D, EntityMapping<E, D>> mappings, LastfmApiCall sourceApiCall) {
        this.mappings = mappings;
        this.sourceApiCall = sourceApiCall;
    }

    public EntityMapping<E, D> get(D dto) {
        return mappings.get(dto);
    }

    public Collection<EntityMapping<E, D>> values() {
        return mappings.values();
    }

    public Map<D, EntityMapping<E, D>> getMap() {
        return mappings;
    }

    public void forEach(BiConsumer<D, EntityMapping<E, D>> consumer) {
        mappings.forEach(consumer);
    }
}
