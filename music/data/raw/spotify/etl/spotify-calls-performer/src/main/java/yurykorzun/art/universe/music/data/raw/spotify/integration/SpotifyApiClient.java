package yurykorzun.art.universe.music.data.raw.spotify.integration;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import yurykorzun.art.universe.data.raw.common.etl.entity.ApiCall;
import yurykorzun.art.universe.data.raw.common.integration.BaseHttpApiClient;

public class SpotifyApiClient extends BaseHttpApiClient {

    private final RestClient restClient;
    private final SpotifyOAuth2TokenProvider tokenProvider;

    public SpotifyApiClient(RestClient restClient, SpotifyOAuth2TokenProvider tokenProvider) {
        this.restClient = restClient;
        this.tokenProvider = tokenProvider;
    }

    @Override
    public String makeApiCall(ApiCall callDetails) {
        String path = callDetails.getType().getPath();
        String spotifyId = callDetails.getParams().get("spotify_id");
        if (spotifyId != null) {
            path = path.replace("{id}", spotifyId);
        }

        String resolvedPath = path;
        return getRestClient().get()
                .uri(uriBuilder -> uriBuilder
                        .path(resolvedPath)
                        .queryParams(getQueryParams(callDetails))
                        .build())
                .header("Authorization", "Bearer " + tokenProvider.getAccessToken())
                .retrieve()
                .body(String.class);
    }

    private MultiValueMap<String, String> getQueryParams(ApiCall callDetails) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        callDetails.getParams().forEach((key, value) -> {
            if (!"spotify_id".equals(key)) {
                params.add(key, value);
            }
        });
        return params;
    }

    @Override
    protected RestClient getRestClient() {
        return this.restClient;
    }
}
