package yurykorzun.art.universe.music.data.raw.spotify.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MasterDataBindingClientTest {

    @Mock private RestClient restClient;
    @Mock private RestClient.RequestBodyUriSpec postUriSpec;
    @Mock(answer = Answers.RETURNS_SELF) private RestClient.RequestBodySpec bodySpec;
    @Mock private RestClient.ResponseSpec responseSpec;

    private MasterDataBindingClient client;

    @BeforeEach
    void setUp() {
        client = new MasterDataBindingClient(restClient);
        when(restClient.post()).thenReturn(postUriSpec);
        when(postUriSpec.uri(anyString(), any(Object.class), any(Object.class))).thenReturn(bodySpec);
        when(bodySpec.retrieve()).thenReturn(responseSpec);
    }

    @Test
    void bindArtistToExisting_shouldPostToArtistsPath() {
        ArgumentCaptor<String> uriCaptor = ArgumentCaptor.forClass(String.class);
        when(postUriSpec.uri(uriCaptor.capture(), any(Object.class), any(Object.class))).thenReturn(bodySpec);

        client.bindArtistToExisting(10L, 20L);

        assertThat(uriCaptor.getValue()).contains("{entityPath}");
        verify(postUriSpec).uri(anyString(), eq("artists"), eq(10L));
    }

    @Test
    void bindAlbumToExisting_shouldPostToAlbumsPath() {
        client.bindAlbumToExisting(11L, 21L);

        verify(postUriSpec).uri(anyString(), eq("albums"), eq(11L));
    }

    @Test
    void bindTrackToExisting_shouldPostToTracksPath() {
        client.bindTrackToExisting(12L, 22L);

        verify(postUriSpec).uri(anyString(), eq("tracks"), eq(12L));
    }

    @Test
    void bindArtistToExisting_shouldRethrow_whenRestClientFails() {
        when(responseSpec.toBodilessEntity()).thenThrow(new RuntimeException("connection refused"));

        assertThatThrownBy(() -> client.bindArtistToExisting(1L, 2L))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("connection refused");
    }
}
