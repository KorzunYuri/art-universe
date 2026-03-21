package yurykorzun.art.universe.music.data.raw.spotify.application;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.StagingIteration;
import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.StagingIterationStatus;
import yurykorzun.art.universe.music.data.raw.spotify.etl.repository.SpotifyStagingIterationRepository;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StagingIterationApplicatorTest {

    @Mock private SpotifyStagingIterationRepository iterationRepository;
    @Mock private JdbcTemplate jdbc;

    @InjectMocks
    private StagingIterationApplicator applicator;

    @Test
    void apply_shouldSetStatusToApplyingThenCompleted_onSuccess() {
        StagingIteration iteration = mock(StagingIteration.class);
        when(iteration.getId()).thenReturn(5L);

        applicator.apply(iteration);

        verify(iteration).setStatus(StagingIterationStatus.APPLYING);
        verify(iteration).setStatus(StagingIterationStatus.COMPLETED);
        // save called twice: once for APPLYING, once for COMPLETED
        verify(iterationRepository, times(2)).save(iteration);
    }

    @Test
    void apply_shouldSetStatusToFailed_whenJdbcThrows() {
        StagingIteration iteration = mock(StagingIteration.class);
        when(iteration.getId()).thenReturn(7L);
        when(jdbc.update(anyString())).thenThrow(new RuntimeException("db error"));

        applicator.apply(iteration);

        verify(iteration).setStatus(StagingIterationStatus.APPLYING);
        verify(iteration).setStatus(StagingIterationStatus.FAILED);
        verify(iteration).setErrorMessage("db error");
    }

    @Test
    void apply_shouldExecuteAllUpsertAndResolveSqlStatements() {
        StagingIteration iteration = mock(StagingIteration.class);
        when(iteration.getId()).thenReturn(9L);

        applicator.apply(iteration);

        // 5 upserts + 3 direct resolves + 4 * 2 resolveEntityIdsInRelation = 16 update calls
        verify(jdbc, times(16)).update(anyString());
    }
}
