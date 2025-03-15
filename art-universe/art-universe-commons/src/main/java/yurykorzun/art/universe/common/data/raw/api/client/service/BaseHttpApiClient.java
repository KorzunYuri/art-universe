package yurykorzun.art.universe.common.data.raw.api.client.service;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import yurykorzun.art.universe.common.data.raw.api.client.entity.ApiCall;

import java.util.Collections;
import java.util.Map;

public abstract class BaseHttpApiClient implements ApiClient {

    @Override
    public Mono<String> makeApiCall(ApiCall callDetails) {
        return getWebClient().get()
                .uri(uriBuilder -> uriBuilder
                        .path(callDetails.getType().getPath())
                        .queryParams(getParamValues(callDetails))
                    .build())
                .retrieve()
                .bodyToMono(String.class);
    }

    protected abstract WebClient getWebClient();

    protected Map<String, String> getDefaultParamValues() {
        return Collections.emptyMap();
    }

    protected MultiValueMap<String, String> getParamValues(ApiCall callDetails) {
        // TODO resolve conflicts and validate provided params against mandatory/optional sets
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        getDefaultParamValues().forEach(params::add);
        callDetails.getType().getDefaultParamValues().forEach(params::add);
        callDetails.getParams().forEach(params::add);
        return params;
    }

}
