package yurykorzun.art.universe.music.data.raw.spotify.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.web.client.RestClient;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SuppressWarnings({"unchecked", "rawtypes"})
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SpotifyOAuth2TokenProviderTest {

    @Mock private RestClient.Builder restClientBuilder;
    @Mock private RestClient restClient;
    @Mock private RestClient.RequestBodyUriSpec postSpec;
    // RETURNS_SELF lets header(), contentType(), body() all return bodySpec without manual stubs
    @Mock(answer = Answers.RETURNS_SELF) private RestClient.RequestBodySpec bodySpec;
    @Mock private RestClient.ResponseSpec responseSpec;

    private SpotifyOAuth2TokenProvider tokenProvider;

    @BeforeEach
    void setUp() {
        when(restClientBuilder.build()).thenReturn(restClient);
        tokenProvider = new SpotifyOAuth2TokenProvider(
            restClientBuilder, "client-id", "client-secret", "https://token.spotify.com");
    }

    private void setupRefreshResponse(Map<String, Object> response) {
        when(restClient.post()).thenReturn(postSpec);
        when(postSpec.uri(anyString())).thenReturn(bodySpec);
        when(bodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(Map.class)).thenReturn(response);
    }

    @Test
    void getAccessToken_shouldRefreshToken_onFirstCall() {
        setupRefreshResponse(Map.of("access_token", "fresh-token", "expires_in", 3600));

        String token = tokenProvider.getAccessToken();

        assertThat(token).isEqualTo("fresh-token");
        verify(restClient).post();
    }

    @Test
    void getAccessToken_shouldReturnCachedToken_whenNotExpired() {
        setupRefreshResponse(Map.of("access_token", "cached-token", "expires_in", 3600));

        String first = tokenProvider.getAccessToken();
        String second = tokenProvider.getAccessToken();

        assertThat(first).isEqualTo("cached-token");
        assertThat(second).isEqualTo("cached-token");
        // refreshToken() is called only once; second call returns cached token
        verify(restClient, times(1)).post();
    }

    @Test
    void getAccessToken_shouldThrow_whenRefreshResponseIsNull() {
        setupRefreshResponse(null);

        assertThatThrownBy(() -> tokenProvider.getAccessToken())
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Failed to obtain Spotify access token");
    }

    @Test
    void getAccessToken_shouldUseBase64BasicAuth_inAuthorizationHeader() {
        setupRefreshResponse(Map.of("access_token", "token", "expires_in", 3600));

        tokenProvider.getAccessToken();

        // client-id:client-secret → Base64 → Authorization: Basic <encoded>
        verify(bodySpec).header(eq("Authorization"), contains("Basic "));
    }
}
