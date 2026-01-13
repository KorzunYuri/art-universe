package yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.impl;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import yurykorzun.art.universe.common.data.raw.api.client.service.BaseHttpApiClient;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.LastfmApiClient;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Component
public class LastfmApiClientImpl extends BaseHttpApiClient implements LastfmApiClient {

    private final RestClient restClient;

    private Map<String, String> defaultParamValues;

    @Value("${lastfm.api.key}")
    private String apiKey;

    public LastfmApiClientImpl(RestClient.Builder restClientBuilder, @Value("${lastfm.api.base-url}") String baseUrl) {
        this.restClient = restClientBuilder
                .baseUrl(baseUrl)
            .build();
    }

    @PostConstruct
    public void init() {
        this.defaultParamValues = Collections.unmodifiableMap(
                new HashMap<>(){{
                    put("api_key", apiKey);
                }}
        );
    }

    @Override
    protected RestClient getRestClient() {
        return this.restClient;
    }

    @Override
    protected Map<String, String> getDefaultParamValues() {
        return this.defaultParamValues;
    }
}
