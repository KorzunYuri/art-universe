package yurykorzun.art.universe.music.data.raw.spotify.task.response.process;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.SpotifyApiCallType;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class SpotifyApiResponseProcessorsRegistry {

    private static final Map<SpotifyApiCallType, Class<? extends BaseSpotifyApiResponseProcessor>> CLASS_REGISTRY = new ConcurrentHashMap<>();
    private static ApplicationContext applicationContext;

    public SpotifyApiResponseProcessorsRegistry(ApplicationContext context) {
        applicationContext = context;
    }

    public static void register(SpotifyApiCallType callType, Class<? extends BaseSpotifyApiResponseProcessor> processorClass) {
        CLASS_REGISTRY.putIfAbsent(callType, processorClass);
        log.debug("Registered processor class {} for call type {}", processorClass.getSimpleName(), callType.getName());
    }

    public static BaseSpotifyApiResponseProcessor get(SpotifyApiCallType callType) {
        Class<? extends BaseSpotifyApiResponseProcessor> processorClass = CLASS_REGISTRY.get(callType);
        if (processorClass == null) {
            return null;
        }
        return applicationContext.getBean(processorClass);
    }

    public static int size() {
        return CLASS_REGISTRY.size();
    }
}
