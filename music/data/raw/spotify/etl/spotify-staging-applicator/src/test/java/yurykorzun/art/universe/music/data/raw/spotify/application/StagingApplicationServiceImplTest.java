package yurykorzun.art.universe.music.data.raw.spotify.application;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.StagingIteration;
import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.StagingIterationStatus;
import yurykorzun.art.universe.music.data.raw.spotify.etl.repository.SpotifyStagingIterationRepository;

import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StagingApplicationServiceImplTest {

    @Mock private SpotifyStagingIterationRepository iterationRepository;
    @Mock private StagingIterationApplicator iterationApplicator;

    @InjectMocks
    private StagingApplicationServiceImpl service;

    @Test
    void applySealedIterations_shouldDoNothing_whenNoSealedIterations() {
        when(iterationRepository.findAllByStatusOrderByOpenedAtAsc(StagingIterationStatus.SEALED))
            .thenReturn(List.of());

        service.applySealedIterations();

        verify(iterationApplicator, never()).apply(any());
    }

    @Test
    void applySealedIterations_shouldApplyEachSealedIteration() {
        StagingIteration iter1 = mock(StagingIteration.class);
        StagingIteration iter2 = mock(StagingIteration.class);
        when(iterationRepository.findAllByStatusOrderByOpenedAtAsc(StagingIterationStatus.SEALED))
            .thenReturn(List.of(iter1, iter2));

        service.applySealedIterations();

        verify(iterationApplicator).apply(iter1);
        verify(iterationApplicator).apply(iter2);
    }
}
