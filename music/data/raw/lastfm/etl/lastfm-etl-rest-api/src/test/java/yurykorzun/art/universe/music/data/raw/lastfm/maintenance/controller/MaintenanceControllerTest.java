package yurykorzun.art.universe.music.data.raw.lastfm.maintenance.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yurykorzun.art.universe.music.data.raw.lastfm.maintenance.exception.MaintenanceException;
import yurykorzun.art.universe.music.data.raw.lastfm.maintenance.service.MaintenanceOrchestrator;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MaintenanceControllerTest {

    @Mock
    private MaintenanceOrchestrator maintenanceOrchestrator;

    @InjectMocks
    private MaintenanceController controller;

    @Test
    void triggerDbMaintenance_shouldReturnSuccessWhenMaintenanceRequested() {
        // Given: Maintenance is not running
        when(maintenanceOrchestrator.requestMaintenance()).thenReturn(true);

        // When: Trigger maintenance
        String result = controller.triggerDbMaintenance();

        // Then: Should return success message
        assertEquals("maintenance requested", result);
        verify(maintenanceOrchestrator, times(1)).requestMaintenance();
    }

    @Test
    void triggerDbMaintenance_shouldThrowExceptionWhenMaintenanceAlreadyRunning() {
        // Given: Maintenance is already running
        when(maintenanceOrchestrator.requestMaintenance()).thenReturn(false);

        // When/Then: Should throw MaintenanceException
        MaintenanceException exception = assertThrows(
            MaintenanceException.class,
            () -> controller.triggerDbMaintenance()
        );

        assertEquals("Maintenance already in progress", exception.getMessage());
        verify(maintenanceOrchestrator, times(1)).requestMaintenance();
    }

    @Test
    void triggerDbMaintenance_shouldCallOrchestratorNotExecutorDirectly() {
        // Given: Maintenance can be requested
        when(maintenanceOrchestrator.requestMaintenance()).thenReturn(true);

        // When: Trigger maintenance
        controller.triggerDbMaintenance();

        // Then: Should call orchestrator's requestMaintenance method
        verify(maintenanceOrchestrator, times(1)).requestMaintenance();
        verifyNoMoreInteractions(maintenanceOrchestrator);
    }
}
