package yurykorzun.art.universe.music.data.raw.spotify.integration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class SpotifyApiClientConfig {

    @Bean
    public SpotifyApiClient spotifyApiClient(
            RestClient.Builder restClientBuilder,
            SpotifyOAuth2TokenProvider tokenProvider,
            @Value("${spotify.api.base-url}") String baseUrl
    ) {
        RestClient restClient = restClientBuilder
                .baseUrl(baseUrl)
                .build();
        return new SpotifyApiClient(restClient, tokenProvider);
    }
}
