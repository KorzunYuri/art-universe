package yurykorzun.art.universe.music.data.raw.spotify.etl.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.SpotifyApiCall;
import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.SpotifyApiCallType;
import yurykorzun.art.universe.music.data.raw.spotify.test.archetypes.SpotifyJpaTestHelper;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SpotifyApiCallRepositoryTest extends SpotifyJpaTestHelper {

    @Autowired
    private SpotifyApiCallRepository repository;

    @Test
    void findAllUnexpiredByType_shouldReturnCallsWithFutureDueDttm() {
        // given
        SpotifyApiCall futureCall = consistencyHelper.createAndSaveApiCall(builder -> builder
            .type(SpotifyApiCallType.ARTIST_GET)
            .dueDttm(Instant.now().plus(1, ChronoUnit.HOURS)));

        consistencyHelper.createAndSaveApiCall(builder -> builder
            .type(SpotifyApiCallType.ARTIST_GET)
            .dueDttm(Instant.now().minus(1, ChronoUnit.HOURS)));

        consistencyHelper.flush();

        // when
        List<SpotifyApiCall> result = repository.findAllUnexpiredByType(SpotifyApiCallType.ARTIST_GET);

        // then
        assertEquals(1, result.size());
        assertEquals(futureCall.getId(), result.get(0).getId());
    }

    @Test
    void findAllUnexpiredByType_shouldFilterByType() {
        // given
        consistencyHelper.createAndSaveApiCall(builder -> builder
            .type(SpotifyApiCallType.ARTIST_GET)
            .dueDttm(Instant.now().plus(1, ChronoUnit.HOURS)));

        consistencyHelper.createAndSaveApiCall(builder -> builder
            .type(SpotifyApiCallType.ALBUM_GET)
            .dueDttm(Instant.now().plus(1, ChronoUnit.HOURS)));

        consistencyHelper.flush();

        // when
        List<SpotifyApiCall> result = repository.findAllUnexpiredByType(SpotifyApiCallType.ARTIST_GET);

        // then
        assertEquals(1, result.size());
        assertEquals(SpotifyApiCallType.ARTIST_GET, result.get(0).getType());
    }

    @Test
    void findAllUnexpiredByType_shouldReturnEmptyList_whenNoCalls() {
        // when
        List<SpotifyApiCall> result = repository.findAllUnexpiredByType(SpotifyApiCallType.ARTIST_GET);

        // then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void findAllUnexpiredByType_shouldOrderByDueDttmAscending() {
        // given
        SpotifyApiCall later = consistencyHelper.createAndSaveApiCall(builder -> builder
            .type(SpotifyApiCallType.ARTIST_GET)
            .dueDttm(Instant.now().plus(2, ChronoUnit.HOURS)));

        SpotifyApiCall earlier = consistencyHelper.createAndSaveApiCall(builder -> builder
            .type(SpotifyApiCallType.ARTIST_GET)
            .dueDttm(Instant.now().plus(30, ChronoUnit.MINUTES)));

        consistencyHelper.flush();

        // when
        List<SpotifyApiCall> result = repository.findAllUnexpiredByType(SpotifyApiCallType.ARTIST_GET);

        // then
        assertEquals(2, result.size());
        assertEquals(earlier.getId(), result.get(0).getId());
        assertEquals(later.getId(), result.get(1).getId());
    }
}
