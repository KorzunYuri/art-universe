package yurykorzun.art.universe.music.data.raw.lastfm.api.client.service;

import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LastfmApiCallGeneratorsRegistry {

    private static final Map<LastfmApiCallType, BaseLastfmApiCallGenerator> REGISTRY = new ConcurrentHashMap<>();

    private LastfmApiCallGeneratorsRegistry() {}

    public static void register(LastfmApiCallType callType, BaseLastfmApiCallGenerator processor) {
        REGISTRY.putIfAbsent(callType, processor);
    }

    public static BaseLastfmApiCallGenerator get(LastfmApiCallType callType) {
        return REGISTRY.get(callType);
    }

    public static Map<LastfmApiCallType, BaseLastfmApiCallGenerator> getRegistry() {
        return Collections.unmodifiableMap(REGISTRY);
    }
}
