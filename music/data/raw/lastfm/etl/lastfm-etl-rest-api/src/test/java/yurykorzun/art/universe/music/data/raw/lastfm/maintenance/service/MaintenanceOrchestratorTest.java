package yurykorzun.art.universe.music.data.raw.lastfm.maintenance.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MaintenanceOrchestratorTest {

    @Mock
    private DbMaintenanceExecutor dbMaintenanceExecutor;

    private MaintenanceOrchestrator orchestrator;

    @BeforeEach
    void setUp() {
        orchestrator = new MaintenanceOrchestrator(dbMaintenanceExecutor);
    }

    @AfterEach
    void tearDown() {
        orchestrator.shutdown();
    }

    @Test
    void requestMaintenance_shouldReturnTrueWhenNotRunning() {
        // When: Request maintenance
        boolean result = orchestrator.requestMaintenance();

        // Then: Should return true
        assertTrue(result, "Should return true when maintenance is not running");
    }

    @Test
    void requestMaintenance_shouldReturnFalseWhenAlreadyRunning() throws InterruptedException {
        // Given: A long-running maintenance operation
        CountDownLatch maintenanceStarted = new CountDownLatch(1);
        CountDownLatch maintenanceCanFinish = new CountDownLatch(1);

        doAnswer(invocation -> {
            maintenanceStarted.countDown();
            maintenanceCanFinish.await(5, TimeUnit.SECONDS);
            return null;
        }).when(dbMaintenanceExecutor).performMaintenance();

        // When: First request
        boolean firstResult = orchestrator.requestMaintenance();

        // Wait for maintenance to actually start
        assertTrue(maintenanceStarted.await(1, TimeUnit.SECONDS), "Maintenance should start");

        // When: Second request while first is still running
        boolean secondResult = orchestrator.requestMaintenance();

        // Then
        assertTrue(firstResult, "First request should return true");
        assertFalse(secondResult, "Second request should return false");
        assertTrue(orchestrator.isMaintenanceInProgress(), "Maintenance should be in progress");

        // Cleanup: Allow maintenance to finish
        maintenanceCanFinish.countDown();

        // Wait for maintenance to complete
        Thread.sleep(100);
        assertFalse(orchestrator.isMaintenanceInProgress(), "Maintenance should be complete");
    }

    @Test
    void requestMaintenance_shouldExecuteMaintenanceAsynchronously() throws InterruptedException {
        // Given: Maintenance executor configured
        CountDownLatch maintenanceExecuted = new CountDownLatch(1);
        doAnswer(invocation -> {
            maintenanceExecuted.countDown();
            return null;
        }).when(dbMaintenanceExecutor).performMaintenance();

        // When: Request maintenance
        boolean result = orchestrator.requestMaintenance();

        // Then: Should return immediately with true
        assertTrue(result);

        // And: Maintenance should execute asynchronously
        assertTrue(maintenanceExecuted.await(2, TimeUnit.SECONDS), "Maintenance should execute within 2 seconds");
        verify(dbMaintenanceExecutor, times(1)).performMaintenance();
    }

    @Test
    void requestMaintenance_shouldClearFlagAfterSuccessfulExecution() throws InterruptedException {
        // Given: Normal maintenance execution
        CountDownLatch maintenanceExecuted = new CountDownLatch(1);
        doAnswer(invocation -> {
            maintenanceExecuted.countDown();
            return null;
        }).when(dbMaintenanceExecutor).performMaintenance();

        // When: Request maintenance
        orchestrator.requestMaintenance();

        // Then: Wait for execution
        assertTrue(maintenanceExecuted.await(2, TimeUnit.SECONDS));

        // Give some time for flag to be cleared
        Thread.sleep(100);

        // And: Flag should be cleared
        assertFalse(orchestrator.isMaintenanceInProgress(), "Flag should be cleared after execution");

        // And: Second request should succeed
        assertTrue(orchestrator.requestMaintenance(), "Second request should succeed after first completes");
    }

    @Test
    void requestMaintenance_shouldClearFlagAfterFailedExecution() throws InterruptedException {
        // Given: Maintenance execution that throws exception
        CountDownLatch maintenanceExecuted = new CountDownLatch(1);
        doAnswer(invocation -> {
            maintenanceExecuted.countDown();
            throw new RuntimeException("Simulated maintenance failure");
        }).when(dbMaintenanceExecutor).performMaintenance();

        // When: Request maintenance
        orchestrator.requestMaintenance();

        // Then: Wait for execution
        assertTrue(maintenanceExecuted.await(2, TimeUnit.SECONDS));

        // Give some time for flag to be cleared
        Thread.sleep(100);

        // And: Flag should be cleared even after failure
        assertFalse(orchestrator.isMaintenanceInProgress(), "Flag should be cleared after failed execution");

        // And: Second request should succeed
        assertTrue(orchestrator.requestMaintenance(), "Second request should succeed after first fails");
    }

    @Test
    void requestMaintenance_shouldAllowMultipleSequentialExecutions() throws InterruptedException {
        // Given: Quick maintenance executions
        CountDownLatch firstExecution = new CountDownLatch(1);
        CountDownLatch secondExecution = new CountDownLatch(1);

        doAnswer(invocation -> {
            if (firstExecution.getCount() > 0) {
                firstExecution.countDown();
            } else {
                secondExecution.countDown();
            }
            return null;
        }).when(dbMaintenanceExecutor).performMaintenance();

        // When: First request
        assertTrue(orchestrator.requestMaintenance());
        assertTrue(firstExecution.await(2, TimeUnit.SECONDS));
        Thread.sleep(100); // Allow flag to be cleared

        // When: Second request after first completes
        assertTrue(orchestrator.requestMaintenance());
        assertTrue(secondExecution.await(2, TimeUnit.SECONDS));

        // Then: Both executions should complete
        verify(dbMaintenanceExecutor, times(2)).performMaintenance();
    }

    @Test
    void scheduledMaintenance_shouldDelegateToRequestMaintenance() throws InterruptedException {
        // Given: Maintenance executor configured
        CountDownLatch maintenanceExecuted = new CountDownLatch(1);
        doAnswer(invocation -> {
            maintenanceExecuted.countDown();
            return null;
        }).when(dbMaintenanceExecutor).performMaintenance();

        // When: Scheduled maintenance triggers
        orchestrator.scheduledMaintenance();

        // Then: Maintenance should execute
        assertTrue(maintenanceExecuted.await(2, TimeUnit.SECONDS));
        verify(dbMaintenanceExecutor, times(1)).performMaintenance();
    }

    @Test
    void isMaintenanceInProgress_shouldReturnTrueWhenRunning() throws InterruptedException {
        // Given: Long-running maintenance
        CountDownLatch maintenanceStarted = new CountDownLatch(1);
        CountDownLatch maintenanceCanFinish = new CountDownLatch(1);

        doAnswer(invocation -> {
            maintenanceStarted.countDown();
            maintenanceCanFinish.await(5, TimeUnit.SECONDS);
            return null;
        }).when(dbMaintenanceExecutor).performMaintenance();

        // When: Request maintenance
        orchestrator.requestMaintenance();
        assertTrue(maintenanceStarted.await(1, TimeUnit.SECONDS));

        // Then: Should report as in progress
        assertTrue(orchestrator.isMaintenanceInProgress());

        // Cleanup
        maintenanceCanFinish.countDown();
    }

    @Test
    void isMaintenanceInProgress_shouldReturnFalseWhenNotRunning() {
        // When: No maintenance requested
        // Then: Should report as not in progress
        assertFalse(orchestrator.isMaintenanceInProgress());
    }

    @Test
    void shutdown_shouldWaitForMaintenanceToComplete() throws InterruptedException {
        // Given: Running maintenance
        CountDownLatch maintenanceStarted = new CountDownLatch(1);
        CountDownLatch maintenanceFinished = new CountDownLatch(1);

        doAnswer(invocation -> {
            maintenanceStarted.countDown();
            Thread.sleep(500); // Simulate work
            maintenanceFinished.countDown();
            return null;
        }).when(dbMaintenanceExecutor).performMaintenance();

        orchestrator.requestMaintenance();
        assertTrue(maintenanceStarted.await(1, TimeUnit.SECONDS));

        // When: Shutdown
        orchestrator.shutdown();

        // Then: Should wait for completion
        assertTrue(maintenanceFinished.await(2, TimeUnit.SECONDS));
        verify(dbMaintenanceExecutor, times(1)).performMaintenance();
    }
}
