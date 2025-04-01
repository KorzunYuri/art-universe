package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.mapping;

import lombok.Getter;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.dto.EntityDto;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.BaseLastfmEntity;

import java.util.Collection;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * A wrapper around EntityMappings map. Translates some basic {@link Map} methods and holds info about source apiCall.
 * @param <E>   entity
 * @param <D>   DTO
 */
public class EntityMappings<E extends BaseLastfmEntity, D extends EntityDto> {
    
    private final Map<String, EntityMapping<E, D>> mappings;

    @Getter
    private final LastfmApiCall sourceApiCall;

    public EntityMappings(Map<String, EntityMapping<E, D>> mappings, LastfmApiCall sourceApiCall) {
        this.mappings = mappings;
        this.sourceApiCall = sourceApiCall;
    }

    public EntityMapping<E, D> get(String uniqueKey) {
        return mappings.get(uniqueKey);
    }

    public Collection<EntityMapping<E, D>> values() {
        return mappings.values();
    }

    public Map<String, EntityMapping<E, D>> getMap() {
        return mappings;
    }

    public void forEach(BiConsumer<String, EntityMapping<E, D>> consumer) {
        mappings.forEach(consumer);
    }
}
