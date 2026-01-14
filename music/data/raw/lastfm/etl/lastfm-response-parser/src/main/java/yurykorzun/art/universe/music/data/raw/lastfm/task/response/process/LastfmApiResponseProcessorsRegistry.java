package yurykorzun.art.universe.music.data.raw.lastfm.task.response.process;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.aspect.ApiResponseProcessorObservabilityAspect;
import yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.DtoRoot;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for LastFM API response processors that stores processor class references
 * and retrieves Spring-managed proxies via ApplicationContext.
 *
 * <p>This approach ensures that AOP aspects (like {@link ApiResponseProcessorObservabilityAspect})
 * properly intercept processor method calls by always returning Spring-managed proxy instances rather than raw instances.</p>
 */
@Component
@Slf4j
public class LastfmApiResponseProcessorsRegistry {

    private static final Map<Class<? extends DtoRoot>, Class<? extends LastfmApiResponseProcessor<? extends DtoRoot>>> CLASS_REGISTRY = new ConcurrentHashMap<>();
    private static ApplicationContext applicationContext;

    public LastfmApiResponseProcessorsRegistry(ApplicationContext context) {
        applicationContext = context;
        log.debug("LastfmApiResponseProcessorsRegistry initialized with ApplicationContext");
    }

    /**
     * Registers a processor class for a specific DTO type.
     * Called from processor constructors during bean initialization.
     *
     * @param dtoClass the DTO class type
     * @param processorClass the processor class (not instance)
     */
    public static <T extends DtoRoot> void register(Class<T> dtoClass, Class<? extends LastfmApiResponseProcessor<T>> processorClass) {
        CLASS_REGISTRY.putIfAbsent(dtoClass, processorClass);
        log.debug("Registered processor class {} for DTO type {}", processorClass.getSimpleName(), dtoClass.getSimpleName());
    }

    /**
     * Retrieves a Spring-managed proxy for the processor of the specified DTO type.
     *
     * @param dtoClass the DTO class type
     * @return the Spring-managed processor proxy (with AOP applied), or null if not found
     */
    @SuppressWarnings("unchecked")
    public static <T extends DtoRoot> LastfmApiResponseProcessor<T> get(Class<T> dtoClass) {
        Class<? extends LastfmApiResponseProcessor<? extends DtoRoot>> processorClass = CLASS_REGISTRY.get(dtoClass);
        if (processorClass == null) {
            return null;
        }
        return (LastfmApiResponseProcessor<T>) applicationContext.getBean(processorClass);
    }

    public static int size() {
        return CLASS_REGISTRY.size();
    }
}
