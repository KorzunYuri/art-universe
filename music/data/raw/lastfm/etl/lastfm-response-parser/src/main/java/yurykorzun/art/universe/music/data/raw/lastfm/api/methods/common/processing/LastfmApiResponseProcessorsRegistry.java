package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing;

import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.dto.DtoRoot;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LastfmApiResponseProcessorsRegistry {

    private static final Map<Class<? extends DtoRoot>, LastfmApiResponseProcessor<? extends DtoRoot>> REGISTRY = new ConcurrentHashMap<>();

    private LastfmApiResponseProcessorsRegistry() {}

    public static <T extends DtoRoot> void  register(Class<T> clazz, LastfmApiResponseProcessor<T> processor) {
        REGISTRY.putIfAbsent(clazz, processor);
    }

    @SuppressWarnings("unchecked")
    public static <T extends DtoRoot> LastfmApiResponseProcessor<T> get(Class<T> clazz) {
        return (LastfmApiResponseProcessor<T>) REGISTRY.get(clazz);
    }

}
