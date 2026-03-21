package yurykorzun.art.universe.music.data.raw.spotify.application;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StagingApplicationSchedulerTest {

    @Mock private StagingApplicationService applicationService;
    @Mock private SearchReconciliationService searchReconciliationService;

    @InjectMocks
    private StagingApplicationScheduler scheduler;

    @Test
    void executeWork_shouldDelegateToBothServices() {
        scheduler.executeWork();

        verify(applicationService).applySealedIterations();
        verify(searchReconciliationService).reconcileMatchedAttempts();
    }

    @Test
    void executeWork_shouldNotPropagateException_whenServiceThrows() {
        doThrow(new RuntimeException("service failure")).when(applicationService).applySealedIterations();

        assertThatNoException().isThrownBy(() -> scheduler.executeWork());
    }
}
