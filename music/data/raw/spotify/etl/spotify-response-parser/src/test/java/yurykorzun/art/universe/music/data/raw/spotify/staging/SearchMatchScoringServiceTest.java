package yurykorzun.art.universe.music.data.raw.spotify.staging;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yurykorzun.art.universe.common.config.client.ConfigPropertyHolder;
import yurykorzun.art.universe.music.data.raw.spotify.config.SpotifyParserProperty;
import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.SearchAttemptStatus;
import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.SpotifySearchAttempt;
import yurykorzun.art.universe.music.data.raw.spotify.etl.repository.SpotifySearchAttemptRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SearchMatchScoringServiceTest {

    @Mock
    private SpotifySearchAttemptRepository searchAttemptRepository;

    @Mock
    private ConfigPropertyHolder configPropertyHolder;

    @InjectMocks
    private SearchMatchScoringService service;

    private void stubThresholdConfig(double threshold) {
        when(configPropertyHolder.getDecimal(SpotifyParserProperty.SEARCH_MATCH_THRESHOLD))
            .thenReturn(BigDecimal.valueOf(threshold));
        when(configPropertyHolder.getInt(SpotifyParserProperty.SEARCH_GRACE_PERIOD_DAYS))
            .thenReturn(30);
    }

    @Test
    void scoreAndUpdate_whenNoAttemptFound_shouldSkipWithoutSaving() {
        when(searchAttemptRepository.findByApiCallId(99L)).thenReturn(Optional.empty());

        service.scoreAndUpdate(99L, List.of("Drake"), List.of("spotify:drake"));

        verify(searchAttemptRepository, never()).save(any());
    }

    @Test
    void scoreAndUpdate_whenExactMatch_shouldSetMatchedStatus() {
        SpotifySearchAttempt attempt = mock(SpotifySearchAttempt.class);
        when(attempt.getSearchString()).thenReturn("Drake");
        when(searchAttemptRepository.findByApiCallId(1L)).thenReturn(Optional.of(attempt));
        stubThresholdConfig(0.85);

        service.scoreAndUpdate(1L, List.of("Drake"), List.of("3TVXtAsR1Inumwj472S9r4"));

        verify(attempt).setStatus(SearchAttemptStatus.MATCHED);
        verify(attempt).setMatchedSpotifyId("3TVXtAsR1Inumwj472S9r4");
        verify(searchAttemptRepository).save(attempt);
    }

    @Test
    void scoreAndUpdate_whenLowSimilarity_shouldSetNoMatchStatus() {
        SpotifySearchAttempt attempt = mock(SpotifySearchAttempt.class);
        when(attempt.getSearchString()).thenReturn("Drake");
        when(searchAttemptRepository.findByApiCallId(1L)).thenReturn(Optional.of(attempt));
        stubThresholdConfig(0.99); // very high threshold — "Drake" vs "ZZZZZ" won't pass

        service.scoreAndUpdate(1L, List.of("ZZZZZ"), List.of("spotify:zzz"));

        verify(attempt).setStatus(SearchAttemptStatus.NO_MATCH);
        verify(attempt).setNextRetryAfter(any());
        verify(searchAttemptRepository).save(attempt);
    }

    @Test
    void scoreAndUpdate_whenEmptyCandidates_shouldSetNoMatchStatus() {
        SpotifySearchAttempt attempt = mock(SpotifySearchAttempt.class);
        when(attempt.getSearchString()).thenReturn("Drake");
        when(searchAttemptRepository.findByApiCallId(1L)).thenReturn(Optional.of(attempt));
        stubThresholdConfig(0.85);

        service.scoreAndUpdate(1L, List.of(), List.of());

        verify(attempt).setStatus(SearchAttemptStatus.NO_MATCH);
        verify(searchAttemptRepository).save(attempt);
    }

    @Test
    void scoreAndUpdate_shouldPickBestScoringCandidate() {
        SpotifySearchAttempt attempt = mock(SpotifySearchAttempt.class);
        when(attempt.getSearchString()).thenReturn("Drake");
        when(searchAttemptRepository.findByApiCallId(1L)).thenReturn(Optional.of(attempt));
        stubThresholdConfig(0.85);

        // "Drake" is an exact match; "drako" is close but not exact
        service.scoreAndUpdate(1L,
            List.of("drako", "Drake"),
            List.of("spotify:drako", "spotify:drake-correct"));

        verify(attempt).setMatchedSpotifyId("spotify:drake-correct");
        verify(attempt).setStatus(SearchAttemptStatus.MATCHED);
    }
}
