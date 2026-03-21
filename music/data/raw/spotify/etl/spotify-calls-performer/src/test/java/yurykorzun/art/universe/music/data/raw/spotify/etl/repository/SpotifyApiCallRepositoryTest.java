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
    void findAllCreatedUnexpired_shouldReturnCreatedCallsWithFutureDueDttm() {
        // given
        SpotifyApiCall createdCall = consistencyHelper.createAndSaveApiCall(builder -> builder
            .type(SpotifyApiCallType.ARTIST_GET)
            .dueDttm(Instant.now().plus(1, ChronoUnit.HOURS)));

        consistencyHelper.flush();

        // when
        List<SpotifyApiCall> result = repository.findAllCreatedUnexpired();

        // then
        assertFalse(result.isEmpty());
        assertTrue(result.stream().anyMatch(c -> c.getId() == createdCall.getId()));
    }

    @Test
    void findAllCreatedUnexpired_shouldNotReturnExpiredCalls() {
        // given — expired call
        consistencyHelper.createAndSaveApiCall(builder -> builder
            .type(SpotifyApiCallType.ARTIST_GET)
            .dueDttm(Instant.now().minus(1, ChronoUnit.HOURS)));

        consistencyHelper.flush();

        // when
        List<SpotifyApiCall> result = repository.findAllCreatedUnexpired();

        // then
        assertTrue(result.isEmpty());
    }

    @Test
    void findAllCreatedUnexpired_shouldOrderByPriorityDescending() {
        // given
        SpotifyApiCall lowPriority = consistencyHelper.createAndSaveApiCall(builder -> builder
            .type(SpotifyApiCallType.ARTIST_GET)
            .dueDttm(Instant.now().plus(1, ChronoUnit.HOURS))
            .priority((short) 0));

        SpotifyApiCall highPriority = consistencyHelper.createAndSaveApiCall(builder -> builder
            .type(SpotifyApiCallType.ALBUM_GET)
            .dueDttm(Instant.now().plus(1, ChronoUnit.HOURS))
            .priority((short) 10));

        consistencyHelper.flush();

        // when
        List<SpotifyApiCall> result = repository.findAllCreatedUnexpired();

        // then
        assertEquals(2, result.size());
        assertEquals(highPriority.getId(), result.get(0).getId());
        assertEquals(lowPriority.getId(), result.get(1).getId());
    }

    @Test
    void findAllCreatedUnexpired_shouldReturnEmptyList_whenNoCalls() {
        // when
        List<SpotifyApiCall> result = repository.findAllCreatedUnexpired();

        // then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
