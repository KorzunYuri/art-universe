package yurykorzun.art.universe.music.data.raw.spotify.application;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.JdbcTemplate;
import yurykorzun.art.universe.music.data.raw.spotify.enums.SpotifyEntityType;
import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.SearchAttemptStatus;
import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.SpotifySearchAttempt;
import yurykorzun.art.universe.music.data.raw.spotify.etl.repository.SpotifySearchAttemptRepository;
import yurykorzun.art.universe.music.data.raw.spotify.integration.MasterDataBindingClient;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SearchReconciliationServiceTest {

    @Mock private SpotifySearchAttemptRepository searchAttemptRepository;
    @Mock private JdbcTemplate jdbc;
    @Mock private MasterDataBindingClient bindingClient;

    @InjectMocks
    private SearchReconciliationService service;

    @Test
    void reconcileMatchedAttempts_shouldDoNothing_whenNoMatchedAttempts() {
        when(searchAttemptRepository.findAllByStatus(SearchAttemptStatus.MATCHED)).thenReturn(List.of());

        service.reconcileMatchedAttempts();

        verify(jdbc, never()).query(anyString(), any(ResultSetExtractor.class), any());
    }

    @Test
    void reconcileMatchedAttempts_shouldProcessEachMatchedAttempt() {
        SpotifySearchAttempt attempt = buildAttempt(SpotifyEntityType.ARTIST, "spotify-123", 10L);
        when(searchAttemptRepository.findAllByStatus(SearchAttemptStatus.MATCHED)).thenReturn(List.of(attempt));
        doReturn(99L).when(jdbc).query(anyString(), any(ResultSetExtractor.class), anyString());

        service.reconcileMatchedAttempts();

        verify(bindingClient).bindArtistToExisting(99L, 10L);
    }

    @Test
    void reconcileAttempt_shouldDoNothing_whenEntityTypeIsUnsupported() {
        SpotifySearchAttempt attempt = buildAttempt(SpotifyEntityType.GENRE, "genre-id", 5L);

        service.reconcileAttempt(attempt);

        verify(jdbc, never()).query(anyString(), any(ResultSetExtractor.class), any());
    }

    @Test
    void reconcileAttempt_shouldDoNothing_whenRawEntityNotFound() {
        SpotifySearchAttempt attempt = buildAttempt(SpotifyEntityType.ARTIST, "spotify-abc", 7L);
        doReturn(null).when(jdbc).query(anyString(), any(ResultSetExtractor.class), anyString());

        service.reconcileAttempt(attempt);

        verify(bindingClient, never()).bindArtistToExisting(anyLong(), anyLong());
        verify(searchAttemptRepository, never()).save(any());
    }

    @Test
    void reconcileAttempt_shouldBindAndMarkBound_forArtist() {
        SpotifySearchAttempt attempt = buildAttempt(SpotifyEntityType.ARTIST, "spotify-artist", 20L);
        doReturn(55L).when(jdbc).query(anyString(), any(ResultSetExtractor.class), anyString());

        service.reconcileAttempt(attempt);

        verify(bindingClient).bindArtistToExisting(55L, 20L);
        verify(searchAttemptRepository).save(attempt);
        assert attempt.getStatus() == SearchAttemptStatus.BOUND;
    }

    @Test
    void reconcileAttempt_shouldBindAndMarkBound_forAlbum() {
        SpotifySearchAttempt attempt = buildAttempt(SpotifyEntityType.ALBUM, "spotify-album", 30L);
        doReturn(66L).when(jdbc).query(anyString(), any(ResultSetExtractor.class), anyString());

        service.reconcileAttempt(attempt);

        verify(bindingClient).bindAlbumToExisting(66L, 30L);
        verify(searchAttemptRepository).save(attempt);
    }

    @Test
    void reconcileAttempt_shouldBindAndMarkBound_forTrack() {
        SpotifySearchAttempt attempt = buildAttempt(SpotifyEntityType.TRACK, "spotify-track", 40L);
        doReturn(77L).when(jdbc).query(anyString(), any(ResultSetExtractor.class), anyString());

        service.reconcileAttempt(attempt);

        verify(bindingClient).bindTrackToExisting(77L, 40L);
        verify(searchAttemptRepository).save(attempt);
    }

    @Test
    void reconcileAttempt_shouldLeaveAsMatched_whenBindingFails() {
        SpotifySearchAttempt attempt = buildAttempt(SpotifyEntityType.ARTIST, "spotify-fail", 99L);
        doReturn(11L).when(jdbc).query(anyString(), any(ResultSetExtractor.class), anyString());
        doThrow(new RuntimeException("binding error")).when(bindingClient).bindArtistToExisting(anyLong(), anyLong());

        service.reconcileAttempt(attempt);

        verify(searchAttemptRepository, never()).save(any());
        // Status remains MATCHED (not updated)
    }

    private SpotifySearchAttempt buildAttempt(SpotifyEntityType entityType, String spotifyId, long masterEntityId) {
        return SpotifySearchAttempt.builder()
            .entityType(entityType)
            .matchedSpotifyId(spotifyId)
            .masterEntityId(masterEntityId)
            .searchString("test")
            .status(SearchAttemptStatus.MATCHED)
            .build();
    }
}
