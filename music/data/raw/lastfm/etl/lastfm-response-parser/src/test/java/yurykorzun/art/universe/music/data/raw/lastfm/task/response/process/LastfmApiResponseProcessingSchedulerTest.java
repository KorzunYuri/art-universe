package yurykorzun.art.universe.music.data.raw.lastfm.task.response.process;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.service.LastfmApiResponseService;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LastfmApiResponseProcessingSchedulerTest {

    @Mock
    private LastfmApiResponseService apiResponseService;

    private LastfmApiResponseProcessingScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new LastfmApiResponseProcessingScheduler(apiResponseService);
    }

    @Test
    void executeWork_shouldCallApiResponseService() {
        scheduler.executeWork();

        verify(apiResponseService).processResponses();
    }

    @Test
    void executeWork_shouldNotPropagateException() {
        doThrow(new RuntimeException("service error")).when(apiResponseService).processResponses();

        scheduler.executeWork();

        verify(apiResponseService).processResponses();
    }
}
