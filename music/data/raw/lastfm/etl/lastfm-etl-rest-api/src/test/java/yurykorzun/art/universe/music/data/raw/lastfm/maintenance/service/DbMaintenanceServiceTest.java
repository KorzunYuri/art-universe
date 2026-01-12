package yurykorzun.art.universe.music.data.raw.lastfm.maintenance.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DbMaintenanceServiceTest {

    @Mock
    private TaskCoordinator taskCoordinator;

    @Mock
    private DbMaintenanceExecutor dbMaintenanceExecutor;

    private DbMaintenanceService service;

    @BeforeEach
    void setUp() {
        service = new DbMaintenanceService(taskCoordinator, dbMaintenanceExecutor);
    }

    @Test
    void enqueueMaintenance_shouldCallTaskCoordinatorWithExecutor() {
        // When: Request maintenance
        boolean result = service.enqueueMaintenance();

        // Then: Verify coordinator was called with executor method reference
        ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(taskCoordinator).requestMaintenance(runnableCaptor.capture());

        // Verify result is true
        assertTrue(result, "enqueueMaintenance should return true on success");

        // Verify the captured runnable executes the executor's method
        Runnable capturedRunnable = runnableCaptor.getValue();
        assertNotNull(capturedRunnable, "Runnable should not be null");

        // Execute the runnable to verify it calls the executor
        capturedRunnable.run();
        verify(dbMaintenanceExecutor).performMaintenance();
    }

    @Test
    void enqueueMaintenance_shouldReturnFalse_whenCoordinatorThrowsException() {
        // Given: Coordinator throws IllegalStateException
        doThrow(new IllegalStateException("Maintenance already in progress"))
            .when(taskCoordinator).requestMaintenance(any(Runnable.class));

        // When: Request maintenance
        boolean result = service.enqueueMaintenance();

        // Then: Verify result is false
        assertFalse(result, "enqueueMaintenance should return false when coordinator throws exception");

        // Verify coordinator was called
        verify(taskCoordinator).requestMaintenance(any(Runnable.class));

        // Verify executor was never called (since coordinator threw exception)
        verify(dbMaintenanceExecutor, never()).performMaintenance();
    }

    @Test
    void enqueueMaintenance_shouldCallExecutorOnlyWhenRunnableIsExecuted() {
        // When: Request maintenance
        service.enqueueMaintenance();

        // Then: Executor should not be called immediately
        verify(dbMaintenanceExecutor, never()).performMaintenance();

        // Capture the runnable
        ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(taskCoordinator).requestMaintenance(runnableCaptor.capture());

        // When: Execute the runnable (simulating coordinator executing it later)
        runnableCaptor.getValue().run();

        // Then: Executor should be called
        verify(dbMaintenanceExecutor).performMaintenance();
    }
}
