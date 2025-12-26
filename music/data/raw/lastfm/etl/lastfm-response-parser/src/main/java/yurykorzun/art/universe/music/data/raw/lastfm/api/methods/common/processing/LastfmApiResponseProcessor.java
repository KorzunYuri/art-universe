package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import yurykorzun.art.universe.common.data.raw.api.methods.common.BaseApiResponseProcessor;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiResponse;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.dto.DtoRoot;

/**
 * Basic class incorporating common logic for all processors of Lastfm API methods responses
 * @param <T> parsed responseBodySubUrl, basically a DTO
 */
public abstract class LastfmApiResponseProcessor<T extends DtoRoot>
        extends BaseApiResponseProcessor<LastfmApiResponse> {

    private final Class<? extends T> clazz;
    protected final ObjectMapper objectMapper;

    protected LastfmApiResponseProcessor(Class<T> clazz, ObjectMapper objectMapper) {
        this.clazz = clazz;
        this.objectMapper = objectMapper;

        LastfmApiResponseProcessorsRegistry.register(clazz, this);
    }

    protected T parseResponse(LastfmApiResponse sourceApiResponse) throws JsonProcessingException {
        return objectMapper.readValue(sourceApiResponse.getResponseBody(), clazz);
    }
}

