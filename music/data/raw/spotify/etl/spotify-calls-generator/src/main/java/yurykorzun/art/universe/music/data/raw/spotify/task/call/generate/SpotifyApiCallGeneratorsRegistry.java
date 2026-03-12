package yurykorzun.art.universe.music.data.raw.spotify.task.call.generate;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.SpotifyApiCallType;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class SpotifyApiCallGeneratorsRegistry {

    private static final Map<SpotifyApiCallType, Class<? extends BaseSpotifyApiCallGenerator>> CLASS_REGISTRY = new ConcurrentHashMap<>();
    private static ApplicationContext applicationContext;

    public SpotifyApiCallGeneratorsRegistry(ApplicationContext context) {
        applicationContext = context;
    }

    public static void register(SpotifyApiCallType callType, Class<? extends BaseSpotifyApiCallGenerator> generatorClass) {
        CLASS_REGISTRY.putIfAbsent(callType, generatorClass);
        log.debug("Registered generator class {} for API call type {}", generatorClass.getSimpleName(), callType.getName());
    }

    public static BaseSpotifyApiCallGenerator get(SpotifyApiCallType callType) {
        Class<? extends BaseSpotifyApiCallGenerator> generatorClass = CLASS_REGISTRY.get(callType);
        if (generatorClass == null) {
            return null;
        }
        return applicationContext.getBean(generatorClass);
    }

    public static Map<SpotifyApiCallType, BaseSpotifyApiCallGenerator> getRegistry() {
        Map<SpotifyApiCallType, BaseSpotifyApiCallGenerator> proxiedRegistry = new LinkedHashMap<>();
        CLASS_REGISTRY.forEach((callType, generatorClass) -> {
            BaseSpotifyApiCallGenerator proxy = applicationContext.getBean(generatorClass);
            proxiedRegistry.put(callType, proxy);
        });
        return Collections.unmodifiableMap(proxiedRegistry);
    }

    public static int size() {
        return CLASS_REGISTRY.size();
    }
}
