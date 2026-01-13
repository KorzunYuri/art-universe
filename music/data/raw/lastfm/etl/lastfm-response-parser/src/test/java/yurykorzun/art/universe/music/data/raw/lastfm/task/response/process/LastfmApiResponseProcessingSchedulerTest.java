package yurykorzun.art.universe.music.data.raw.lastfm.task.response.process;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.service.LastfmApiResponseService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

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
    void triggerResponsesProcessing_shouldCallApiResponseService() {
        // When
        scheduler.triggerResponsesProcessing();

        // Then
        verify(apiResponseService).processResponses();
    }

    @Test
    void triggerResponsesProcessing_shouldThrow_whenResponseServiceThrows() {
        // Given
        final var expectedMessage = "test";
        doThrow(new IllegalArgumentException(expectedMessage))
            .when(apiResponseService).processResponses();

        // When
        IllegalArgumentException actualException = assertThrows(
            IllegalArgumentException.class,
            () -> scheduler.triggerResponsesProcessing());

        // Then
        assertEquals(expectedMessage, actualException.getMessage());
    }
}
