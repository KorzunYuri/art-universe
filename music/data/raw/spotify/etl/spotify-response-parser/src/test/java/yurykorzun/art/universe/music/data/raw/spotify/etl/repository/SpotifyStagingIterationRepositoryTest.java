package yurykorzun.art.universe.music.data.raw.spotify.etl.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.StagingIteration;
import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.StagingIterationStatus;
import yurykorzun.art.universe.music.data.raw.spotify.test.archetypes.SpotifyJpaTestHelper;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class SpotifyStagingIterationRepositoryTest extends SpotifyJpaTestHelper {

    @Autowired
    private SpotifyStagingIterationRepository repository;

    @Test
    void findFirstByStatusOrderByOpenedAtAsc_shouldReturnOpenIteration() {
        // given
        consistencyHelper.createAndSaveStagingIteration();
        consistencyHelper.flush();

        // when
        Optional<StagingIteration> result = repository.findFirstByStatusOrderByOpenedAtAsc(StagingIterationStatus.OPEN);

        // then
        assertTrue(result.isPresent());
        assertEquals(StagingIterationStatus.OPEN, result.get().getStatus());
    }

    @Test
    void findFirstByStatusOrderByOpenedAtAsc_shouldReturnEmpty_whenNoOpenIteration() {
        // when
        Optional<StagingIteration> result = repository.findFirstByStatusOrderByOpenedAtAsc(StagingIterationStatus.OPEN);

        // then
        assertTrue(result.isEmpty());
    }

    @Test
    void findFirstByStatusOrderByOpenedAtAsc_shouldReturnEarliest_whenMultipleOpenIterations() {
        // given
        StagingIteration older = StagingIteration.builder()
            .openedAt(Instant.now().minus(2, ChronoUnit.HOURS))
            .build();
        StagingIteration newer = StagingIteration.builder()
            .openedAt(Instant.now().minus(30, ChronoUnit.MINUTES))
            .build();
        repository.save(older);
        repository.save(newer);
        consistencyHelper.flush();

        // when
        Optional<StagingIteration> result = repository.findFirstByStatusOrderByOpenedAtAsc(StagingIterationStatus.OPEN);

        // then
        assertTrue(result.isPresent());
        assertEquals(older.getId(), result.get().getId());
    }

    @Test
    void findFirstByStatusOrderByOpenedAtAsc_shouldNotReturnSealedIteration() {
        // given
        StagingIteration sealed = StagingIteration.builder()
            .status(StagingIterationStatus.SEALED)
            .build();
        repository.save(sealed);
        consistencyHelper.flush();

        // when
        Optional<StagingIteration> result = repository.findFirstByStatusOrderByOpenedAtAsc(StagingIterationStatus.OPEN);

        // then
        assertTrue(result.isEmpty());
    }
}
