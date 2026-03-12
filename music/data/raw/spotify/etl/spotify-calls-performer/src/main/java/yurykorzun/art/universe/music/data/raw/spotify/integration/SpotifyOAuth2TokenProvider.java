package yurykorzun.art.universe.music.data.raw.spotify.integration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;

@Component
public class SpotifyOAuth2TokenProvider {

    private final RestClient restClient;
    private final String clientId;
    private final String clientSecret;
    private final String tokenUrl;

    private volatile String cachedToken;
    private volatile Instant tokenExpiry = Instant.MIN;

    public SpotifyOAuth2TokenProvider(
            RestClient.Builder restClientBuilder,
            @Value("${spotify.api.client-id}") String clientId,
            @Value("${spotify.api.client-secret}") String clientSecret,
            @Value("${spotify.api.token-url}") String tokenUrl
    ) {
        this.restClient = restClientBuilder.build();
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.tokenUrl = tokenUrl;
    }

    public synchronized String getAccessToken() {
        if (cachedToken != null && Instant.now().isBefore(tokenExpiry)) {
            return cachedToken;
        }
        return refreshToken();
    }

    @SuppressWarnings("unchecked")
    private String refreshToken() {
        String credentials = Base64.getEncoder().encodeToString(
                (clientId + ":" + clientSecret).getBytes(StandardCharsets.UTF_8));

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");

        Map<String, Object> response = restClient.post()
                .uri(tokenUrl)
                .header(HttpHeaders.AUTHORIZATION, "Basic " + credentials)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(body)
                .retrieve()
                .body(Map.class);

        if (response == null) {
            throw new IllegalStateException("Failed to obtain Spotify access token");
        }

        cachedToken = (String) response.get("access_token");
        int expiresIn = (Integer) response.get("expires_in");
        tokenExpiry = Instant.now().plusSeconds(expiresIn - 60);

        return cachedToken;
    }
}
