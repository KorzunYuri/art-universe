package yurykorzun.art.universe.music.data.raw.spotify.task.call.generate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import yurykorzun.art.universe.music.data.raw.spotify.domain.entity.SpotifyArtist;
import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.SpotifyApiCallType;
import yurykorzun.art.universe.music.data.raw.spotify.test.archetypes.SpotifyJpaTestHelper;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Import(SpotifyApiCallEntityServiceImpl.class)
class SpotifyApiCallEntityServiceImplTest extends SpotifyJpaTestHelper {

    @Autowired
    private SpotifyApiCallEntityServiceImpl service;

    @Test
    void findAllWithoutActiveCalls_shouldReturnEmpty_whenNoArtistsExist() {
        List<SpotifyArtist> result = service.findAllWithoutActiveCalls(SpotifyArtist.class, SpotifyApiCallType.ARTIST_GET);

        assertThat(result).isEmpty();
    }

    @Test
    void findAllWithoutActiveCalls_shouldReturnArtist_whenNoApiCallExists() {
        SpotifyArtist artist = consistencyHelper.createAndSaveArtist();
        consistencyHelper.flush();

        List<SpotifyArtist> result = service.findAllWithoutActiveCalls(SpotifyArtist.class, SpotifyApiCallType.ARTIST_GET);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(artist.getId());
    }

    @Test
    void findAllWithoutActiveCalls_shouldExcludeArtist_whenActiveApiCallExists() {
        SpotifyArtist artist = consistencyHelper.createAndSaveArtist();
        consistencyHelper.createAndSaveApiCall(builder -> builder
            .type(SpotifyApiCallType.ARTIST_GET)
            .spotifyId(artist.getSpotifyId())
            .dueDttm(Instant.now().plus(1, ChronoUnit.HOURS)));
        consistencyHelper.flush();

        List<SpotifyArtist> result = service.findAllWithoutActiveCalls(SpotifyArtist.class, SpotifyApiCallType.ARTIST_GET);

        assertThat(result).isEmpty();
    }

    @Test
    void findAllWithoutActiveCalls_shouldReturnArtist_whenApiCallIsExpired() {
        SpotifyArtist artist = consistencyHelper.createAndSaveArtist();
        consistencyHelper.createAndSaveApiCall(builder -> builder
            .type(SpotifyApiCallType.ARTIST_GET)
            .spotifyId(artist.getSpotifyId())
            .dueDttm(Instant.now().minus(1, ChronoUnit.HOURS)));
        consistencyHelper.flush();

        List<SpotifyArtist> result = service.findAllWithoutActiveCalls(SpotifyArtist.class, SpotifyApiCallType.ARTIST_GET);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(artist.getId());
    }

    @Test
    void findAllWithoutActiveCalls_shouldReturnArtist_whenCallExistsForDifferentType() {
        SpotifyArtist artist = consistencyHelper.createAndSaveArtist();
        consistencyHelper.createAndSaveApiCall(builder -> builder
            .type(SpotifyApiCallType.ALBUM_GET)
            .spotifyId(artist.getSpotifyId())
            .dueDttm(Instant.now().plus(1, ChronoUnit.HOURS)));
        consistencyHelper.flush();

        List<SpotifyArtist> result = service.findAllWithoutActiveCalls(SpotifyArtist.class, SpotifyApiCallType.ARTIST_GET);

        assertThat(result).hasSize(1);
    }
}
