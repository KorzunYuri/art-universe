package yurykorzun.art.universe.music.data.raw.spotify.staging;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import yurykorzun.art.universe.music.data.raw.spotify.enums.SpotifyEntityType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SyntheticIdResolutionServiceTest {

    @Mock
    private JdbcTemplate jdbc;

    @InjectMocks
    private SyntheticIdResolutionService service;

    @Test
    void resolveId_shouldReturnRealId_whenEntityExistsInDb() {
        when(jdbc.query(anyString(), any(ResultSetExtractor.class), eq("artist-123")))
            .thenReturn(42L);

        long id = service.resolveId(SpotifyEntityType.ARTIST, "artist-123");

        assertThat(id).isEqualTo(42L);
    }

    @Test
    void resolveId_shouldReturnNegativeSyntheticId_whenEntityNotInDb() {
        when(jdbc.query(anyString(), any(ResultSetExtractor.class), eq("unknown-id")))
            .thenReturn(null);

        long id = service.resolveId(SpotifyEntityType.ARTIST, "unknown-id");

        assertThat(id).isNegative();
    }

    @ParameterizedTest
    @EnumSource(value = SpotifyEntityType.class, names = {"ARTIST", "ALBUM", "TRACK", "GENRE"})
    void resolveId_shouldReturnNegativeId_forAllEntityTypes_whenNotFound(SpotifyEntityType entityType) {
        when(jdbc.query(anyString(), any(ResultSetExtractor.class), any()))
            .thenReturn(null);

        long id = service.resolveId(entityType, "some-spotify-id");

        assertThat(id).isNegative();
    }

    @Test
    void resolveId_syntheticIds_shouldBeDeterministic() {
        when(jdbc.query(anyString(), any(ResultSetExtractor.class), any()))
            .thenReturn(null);

        long id1 = service.resolveId(SpotifyEntityType.ARTIST, "same-id");
        long id2 = service.resolveId(SpotifyEntityType.ARTIST, "same-id");

        assertThat(id1).isEqualTo(id2).isNegative();
    }

    @Test
    void resolveId_syntheticIds_shouldDifferByEntityType() {
        when(jdbc.query(anyString(), any(ResultSetExtractor.class), any()))
            .thenReturn(null);

        long artistId = service.resolveId(SpotifyEntityType.ARTIST, "same-spotify-id");
        long albumId = service.resolveId(SpotifyEntityType.ALBUM, "same-spotify-id");

        assertThat(artistId).isNotEqualTo(albumId);
    }
}
