package yurykorzun.art.universe.music.data.raw.spotify.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriComponentsBuilder;
import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.SpotifyApiCall;
import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.SpotifyApiCallType;

import java.net.URI;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SuppressWarnings({"unchecked", "rawtypes"})
@ExtendWith(MockitoExtension.class)
class SpotifyApiClientTest {

    @Mock private RestClient restClient;
    @Mock private SpotifyOAuth2TokenProvider tokenProvider;
    @Mock private RestClient.RequestHeadersUriSpec requestHeadersUriSpec;
    @Mock private RestClient.RequestHeadersSpec requestHeadersSpec;
    @Mock private RestClient.ResponseSpec responseSpec;

    private SpotifyApiClient client;

    @BeforeEach
    void setUp() {
        client = new SpotifyApiClient(restClient, tokenProvider);
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        lenient().when(requestHeadersSpec.header(anyString(), anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(String.class)).thenReturn("{}");
        when(tokenProvider.getAccessToken()).thenReturn("test-token");
    }

    @Test
    void makeApiCall_shouldReplaceIdInPath_whenSpotifyIdPresent() {
        SpotifyApiCall call = SpotifyApiCall.builder()
            .type(SpotifyApiCallType.ARTIST_GET)
            .spotifyId("artist-abc")
            .params(Map.of("spotify_id", "artist-abc"))
            .dueDttm(Instant.now().plus(1, ChronoUnit.DAYS))
            .build();

        ArgumentCaptor<Function<UriBuilder, URI>> uriCaptor = ArgumentCaptor.forClass(Function.class);
        when(requestHeadersUriSpec.uri(uriCaptor.capture())).thenReturn(requestHeadersSpec);

        client.makeApiCall(call);

        URI uri = uriCaptor.getValue().apply(UriComponentsBuilder.newInstance());
        assertThat(uri.getPath()).isEqualTo("/artists/artist-abc");
    }

    @Test
    void makeApiCall_shouldNotReplaceIdInPath_whenNoSpotifyIdParam() {
        SpotifyApiCall call = SpotifyApiCall.builder()
            .type(SpotifyApiCallType.SEARCH_ARTIST)
            .params(Map.of("q", "Drake", "type", "artist"))
            .dueDttm(Instant.now().plus(1, ChronoUnit.DAYS))
            .build();

        ArgumentCaptor<Function<UriBuilder, URI>> uriCaptor = ArgumentCaptor.forClass(Function.class);
        when(requestHeadersUriSpec.uri(uriCaptor.capture())).thenReturn(requestHeadersSpec);

        client.makeApiCall(call);

        URI uri = uriCaptor.getValue().apply(UriComponentsBuilder.newInstance());
        assertThat(uri.getPath()).isEqualTo("/search");
    }

    @Test
    void makeApiCall_shouldExcludeSpotifyIdFromQueryParams() {
        SpotifyApiCall call = SpotifyApiCall.builder()
            .type(SpotifyApiCallType.ARTIST_GET)
            .spotifyId("artist-abc")
            .params(Map.of("spotify_id", "artist-abc", "extra", "value"))
            .dueDttm(Instant.now().plus(1, ChronoUnit.DAYS))
            .build();

        ArgumentCaptor<Function<UriBuilder, URI>> uriCaptor = ArgumentCaptor.forClass(Function.class);
        when(requestHeadersUriSpec.uri(uriCaptor.capture())).thenReturn(requestHeadersSpec);

        client.makeApiCall(call);

        URI uri = uriCaptor.getValue().apply(UriComponentsBuilder.newInstance());
        String query = uri.getQuery();
        assertThat(query).doesNotContain("spotify_id");
        assertThat(query).contains("extra=value");
    }

    @Test
    void makeApiCall_shouldAddBearerTokenInAuthorizationHeader() {
        SpotifyApiCall call = SpotifyApiCall.builder()
            .type(SpotifyApiCallType.ARTIST_GET)
            .spotifyId("artist-abc")
            .params(Map.of("spotify_id", "artist-abc"))
            .dueDttm(Instant.now().plus(1, ChronoUnit.DAYS))
            .build();
        when(tokenProvider.getAccessToken()).thenReturn("my-secret-token");

        ArgumentCaptor<String> headerValueCaptor = ArgumentCaptor.forClass(String.class);
        when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.header(eq("Authorization"), headerValueCaptor.capture())).thenReturn(requestHeadersSpec);

        client.makeApiCall(call);

        assertThat(headerValueCaptor.getValue()).isEqualTo("Bearer my-secret-token");
    }
}
