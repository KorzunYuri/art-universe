package yurykorzun.art.universe.music.data.raw.lastfm.task.call.generate;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.task.call.generate.aspect.ApiCallGeneratorObservabilityAspect;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for LastFM API call generators that stores class references and retrieves
 * Spring-managed proxies via ApplicationContext.
 *
 * <p>This approach ensures that AOP aspects (like {@link ApiCallGeneratorObservabilityAspect})
 * properly intercept generator method calls by always returning Spring-managed proxy instances rather than raw instances.</p>
 */
@Component
@Slf4j
public class LastfmApiCallGeneratorsRegistry {

    private static final Map<LastfmApiCallType, Class<? extends BaseLastfmApiCallGenerator>> CLASS_REGISTRY = new ConcurrentHashMap<>();
    private static ApplicationContext applicationContext;

    public LastfmApiCallGeneratorsRegistry(ApplicationContext context) {
        applicationContext = context;
    }

    /**
     * Registers a generator class for a specific API call type.
     * Called from generator constructors during bean initialization.
     *
     * @param callType the API call type
     * @param generatorClass the generator class (not instance)
     */
    public static void register(LastfmApiCallType callType, Class<? extends BaseLastfmApiCallGenerator> generatorClass) {
        CLASS_REGISTRY.putIfAbsent(callType, generatorClass);
        log.debug("Registered generator class {} for API call type {}", generatorClass.getSimpleName(), callType.getName());
    }

    /**
     * Retrieves a Spring-managed proxy for the generator of the specified API call type.
     *
     * @param callType the API call type
     * @return the Spring-managed generator proxy (with AOP applied), or null if not found
     */
    public static BaseLastfmApiCallGenerator get(LastfmApiCallType callType) {
        Class<? extends BaseLastfmApiCallGenerator> generatorClass = CLASS_REGISTRY.get(callType);
        if (generatorClass == null) {
            return null;
        }
        return applicationContext.getBean(generatorClass);
    }

    /**
     * Returns a map of all registered generators as Spring-managed proxies.
     * Use this method to iterate over all generators with AOP applied.
     *
     * @return unmodifiable map of API call type to Spring-managed generator proxy
     */
    public static Map<LastfmApiCallType, BaseLastfmApiCallGenerator> getRegistry() {
        Map<LastfmApiCallType, BaseLastfmApiCallGenerator> proxiedRegistry = new LinkedHashMap<>();
        CLASS_REGISTRY.forEach((callType, generatorClass) -> {
            BaseLastfmApiCallGenerator proxy = applicationContext.getBean(generatorClass);
            proxiedRegistry.put(callType, proxy);
        });
        return Collections.unmodifiableMap(proxiedRegistry);
    }

    public static int size() {
        return CLASS_REGISTRY.size();
    }
}
