package yurykorzun.art.universe.music.data.raw.spotify.etl.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yurykorzun.art.universe.common.pgnotify.PgNotifyEventPublisher;
import yurykorzun.art.universe.music.data.raw.spotify.common.SpotifyConstants;
import yurykorzun.art.universe.music.data.raw.spotify.etl.dto.SpotifyApiCallCreateRequest;
import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.SpotifyApiCall;
import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.SpotifyApiCallType;
import yurykorzun.art.universe.music.data.raw.spotify.etl.repository.SpotifyApiCallRepository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.mockito.ArgumentCaptor;
import yurykorzun.art.universe.music.data.raw.spotify.enums.SpotifyEntityType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpotifyApiCallServiceImplTest {

    @Mock
    private SpotifyApiCallRepository apiCallRepository;

    @Mock
    private PgNotifyEventPublisher pgNotifyEventPublisher;

    @InjectMocks
    private SpotifyApiCallServiceImpl service;

    private SpotifyApiCallCreateRequest createRequest() {
        return SpotifyApiCallCreateRequest.builder()
            .type(SpotifyApiCallType.ARTIST_GET)
            .spotifyId("3TVXtAsR1Inumwj472S9r4")
            .entityType(SpotifyEntityType.ARTIST)
            .entityId(1L)
            .dueDttm(Instant.now().plus(7, ChronoUnit.DAYS))
            .build();
    }

    @Test
    void createApiCalls_shouldNotifyAfterCommit_whenCallsAreSaved() {
        // given
        SpotifyApiCall savedCall = SpotifyApiCall.builder()
            .type(SpotifyApiCallType.ARTIST_GET)
            .spotifyId("3TVXtAsR1Inumwj472S9r4")
            .dueDttm(Instant.now())
            .build();
        when(apiCallRepository.saveAll(any())).thenReturn(List.of(savedCall));

        // when
        service.createApiCalls(List.of(createRequest()));

        // then
        verify(pgNotifyEventPublisher).notifyAfterCommit(SpotifyConstants.NOTIFY_CALLS_READY);
    }

    @Test
    void createApiCalls_shouldNotNotify_whenNoCallsAreSaved() {
        // given
        when(apiCallRepository.saveAll(any())).thenReturn(List.of());

        // when
        service.createApiCalls(List.of());

        // then
        verifyNoInteractions(pgNotifyEventPublisher);
    }

    @Test
    @SuppressWarnings("unchecked")
    void createApiCalls_shouldMapRequestToEntityCorrectly() {
        SpotifyApiCall savedCall = mock(SpotifyApiCall.class);
        when(apiCallRepository.saveAll(any())).thenReturn(List.of(savedCall));

        ArgumentCaptor<List<SpotifyApiCall>> captor = ArgumentCaptor.forClass(List.class);
        service.createApiCalls(List.of(createRequest()));

        verify(apiCallRepository).saveAll(captor.capture());
        SpotifyApiCall built = captor.getValue().get(0);
        assertThat(built.getType()).isEqualTo(SpotifyApiCallType.ARTIST_GET);
        assertThat(built.getSpotifyId()).isEqualTo("3TVXtAsR1Inumwj472S9r4");
        assertThat(built.getEntityType()).isEqualTo(SpotifyEntityType.ARTIST);
        assertThat(built.getEntityId()).isEqualTo(1L);
    }

    @Test
    @SuppressWarnings("unchecked")
    void createApiCalls_shouldDefaultParamsToSpotifyId_whenParamsNotSet() {
        SpotifyApiCall savedCall = mock(SpotifyApiCall.class);
        when(apiCallRepository.saveAll(any())).thenReturn(List.of(savedCall));

        ArgumentCaptor<List<SpotifyApiCall>> captor = ArgumentCaptor.forClass(List.class);
        service.createApiCalls(List.of(createRequest()));

        verify(apiCallRepository).saveAll(captor.capture());
        assertThat(captor.getValue().get(0).getParams())
            .containsEntry("spotify_id", "3TVXtAsR1Inumwj472S9r4");
    }
}
