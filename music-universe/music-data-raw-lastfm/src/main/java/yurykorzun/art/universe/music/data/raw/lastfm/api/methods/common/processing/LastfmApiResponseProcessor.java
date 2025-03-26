package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import yurykorzun.art.universe.common.data.raw.api.methods.common.BaseApiResponseProcessor;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiResponse;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.common.dto.RootDto;

/**
 * Basic class incorporating common logic for all processors of Lastfm API methods responses
 * @param <T> parsed responseBody, basically a DTO
 */
public abstract class LastfmApiResponseProcessor<T extends RootDto>
        extends BaseApiResponseProcessor<LastfmApiResponse> {

    private final Class<? extends T> clazz;
    protected final ObjectMapper objectMapper = new ObjectMapper();

    protected LastfmApiResponseProcessor(Class<T> clazz) {
        this.clazz = clazz;

        LastfmApiResponseProcessorsRegistry.register(clazz, this);
    }

    protected T parseResponse(LastfmApiResponse response) throws JsonProcessingException {
        return objectMapper.readValue(response.getResponseBody(), clazz);
    }
}

