package yurykorzun.art.universe.music.data.raw.lastfm.task.response.process;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmApiResponse;
import yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.DtoRoot;

/**
 * Basic class incorporating common logic for all processors of Lastfm API methods responses.
 *
 * <p>Processors self-register in the registry during construction by registering their
 * class type (not instance). This allows the registry to retrieve Spring-managed proxies
 * via ApplicationContext, ensuring AOP aspects (like timing metrics) work correctly.</p>
 *
 * @param <T> parsed responseBodySubUrl, basically a DTO
 * @see LastfmApiResponseProcessorsRegistry
 */
public abstract class LastfmApiResponseProcessor<T extends DtoRoot>
        extends BaseApiResponseProcessor<LastfmApiResponse> {

    private final Class<? extends T> clazz;
    protected final ObjectMapper objectMapper;

    /**
     * Constructor that self-registers this processor class in the registry.
     * Registers the processor class type (not instance) to allow registry to retrieve
     * Spring-managed proxies with AOP applied.
     *
     * @param clazz the DTO class type this processor handles
     * @param objectMapper the Jackson ObjectMapper for JSON parsing
     */
    @SuppressWarnings("unchecked")
    protected LastfmApiResponseProcessor(Class<T> clazz, ObjectMapper objectMapper) {
        this.clazz = clazz;
        this.objectMapper = objectMapper;

        // Register the processor class (not instance) for AOP proxy support
        LastfmApiResponseProcessorsRegistry.register(clazz, (Class<? extends LastfmApiResponseProcessor<T>>) this.getClass());
    }

    /**
     * Parses the JSON response body into the DTO type.
     *
     * @param sourceApiResponse the API response containing JSON body
     * @return the parsed DTO
     * @throws JsonProcessingException if JSON parsing fails
     */
    protected T parseResponse(LastfmApiResponse sourceApiResponse) throws JsonProcessingException {
        return objectMapper.readValue(sourceApiResponse.getResponseBody(), clazz);
    }
}

