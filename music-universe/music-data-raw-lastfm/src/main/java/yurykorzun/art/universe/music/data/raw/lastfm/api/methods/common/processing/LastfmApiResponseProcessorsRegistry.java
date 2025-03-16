package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing;

import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.common.dto.RootDto;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LastfmApiResponseProcessorsRegistry {

    private static final Map<Class<? extends RootDto>, LastfmApiResponseProcessor<? extends RootDto>> REGISTRY = new ConcurrentHashMap<>();

    private LastfmApiResponseProcessorsRegistry() {}

    public static <T extends RootDto> void  register(Class<T> clazz, LastfmApiResponseProcessor<T> processor) {
        REGISTRY.putIfAbsent(clazz, processor);
    }

    @SuppressWarnings("unchecked")
    public static <T extends RootDto> LastfmApiResponseProcessor<T> get(Class<T> clazz) {
        return (LastfmApiResponseProcessor<T>) REGISTRY.get(clazz);
    }

}
