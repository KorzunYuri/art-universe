package yurykorzun.art.universe.music.data.raw.lastfm.api.client.scheduling;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.LastfmApiCallService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LastfmApiCallSchedulerTest {

    @Mock
    private LastfmApiCallService lastfmApiCallService;

    private LastfmApiCallExecutionScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new LastfmApiCallExecutionScheduler(lastfmApiCallService);
    }

    @Test
    void triggerApiCalls_shouldCallService() {
        // when
        scheduler.triggerApiCallsExecution();

        // then
        verify(lastfmApiCallService).executeApiCalls();
    }

    @Test
    void triggerApiCalls_shouldThrow_whenServiceThrows() {
        // Given
        final var expectedMessage = "test";
        doThrow(new IllegalArgumentException(expectedMessage))
            .when(lastfmApiCallService).executeApiCalls();

        // When
        IllegalArgumentException actualException = assertThrows(
            IllegalArgumentException.class,
            () -> scheduler.triggerApiCallsExecution());

        // Then
        assertEquals(expectedMessage, actualException.getMessage());
    }
}
