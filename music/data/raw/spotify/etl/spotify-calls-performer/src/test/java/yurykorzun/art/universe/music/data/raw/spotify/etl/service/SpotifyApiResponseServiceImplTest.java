package yurykorzun.art.universe.music.data.raw.spotify.etl.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.SpotifyApiCall;
import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.SpotifyApiResponse;
import yurykorzun.art.universe.music.data.raw.spotify.etl.repository.SpotifyApiResponseRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SpotifyApiResponseServiceImplTest {

    @Mock
    private SpotifyApiResponseRepository apiResponseRepository;

    @InjectMocks
    private SpotifyApiResponseServiceImpl service;

    @Test
    void createResponse_shouldSaveResponseAndReturnId() {
        SpotifyApiCall call = mock(SpotifyApiCall.class);
        SpotifyApiResponse savedResponse = mock(SpotifyApiResponse.class);
        when(savedResponse.getId()).thenReturn(42L);
        when(apiResponseRepository.save(any(SpotifyApiResponse.class))).thenReturn(savedResponse);

        long id = service.createResponse(call, "{\"name\": \"Drake\"}");

        assertThat(id).isEqualTo(42L);
    }

    @Test
    void createResponse_shouldSaveWithCorrectCallAndBody() {
        SpotifyApiCall call = mock(SpotifyApiCall.class);
        SpotifyApiResponse savedResponse = mock(SpotifyApiResponse.class);
        when(savedResponse.getId()).thenReturn(1L);
        when(apiResponseRepository.save(any(SpotifyApiResponse.class))).thenReturn(savedResponse);

        ArgumentCaptor<SpotifyApiResponse> captor = ArgumentCaptor.forClass(SpotifyApiResponse.class);

        service.createResponse(call, "{\"name\": \"Drake\"}");

        verify(apiResponseRepository).save(captor.capture());
        SpotifyApiResponse saved = captor.getValue();
        assertThat(saved.getApiCall()).isSameAs(call);
        assertThat(saved.getResponseBody()).isEqualTo("{\"name\": \"Drake\"}");
    }
}
