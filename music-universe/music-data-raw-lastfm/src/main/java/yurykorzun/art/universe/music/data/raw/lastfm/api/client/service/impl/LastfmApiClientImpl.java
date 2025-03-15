package yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.impl;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import yurykorzun.art.universe.common.data.raw.api.client.service.BaseHttpApiClient;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.LastfmApiClient;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Component
public class LastfmApiClientImpl extends BaseHttpApiClient implements LastfmApiClient {

    private final WebClient webClient;

    private Map<String, String> defaultParamValues;

    @Value("lastfm.apikey")
    private String apiKey;

    public LastfmApiClientImpl(WebClient.Builder webClientBuilder, @Value("lastfm.baseUrl") String baseUrl) {
        this.webClient = webClientBuilder
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
    protected WebClient getWebClient() {
        return this.webClient;
    }

    @Override
    protected Map<String, String> getDefaultParamValues() {
        return this.defaultParamValues;
    }
}
