package yurykorzun.art.universe.music.data.raw.lastfm.maintenance.provider;

import org.junit.jupiter.api.BeforeEach;
import yurykorzun.art.universe.music.data.raw.lastfm.coordination.entity.CoordinationStatus;
import org.junit.jupiter.api.Test;
import yurykorzun.art.universe.music.data.raw.lastfm.maintenance.coordination.provider.InMemoryCoordinationProvider;
import yurykorzun.art.universe.music.data.raw.lastfm.maintenance.coordination.TaskExecutionTicket;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InMemoryCoordinationProviderTest {

    private InMemoryCoordinationProvider provider;

    @BeforeEach
    void setUp() {
        provider = new InMemoryCoordinationProvider();
    }

    @Test
    void shouldAcquireTaskWhenStatusIsNormal() {
        // Given
        String taskKey = "test-task";
        String instanceId = "instance-1";

        // When
        Optional<TaskExecutionTicket> ticket = provider.tryAcquireTaskExecution(taskKey, instanceId);

        // Then
        assertThat(ticket).isPresent();
        assertThat(ticket.get().getTaskKey()).isEqualTo(taskKey);
        assertThat(ticket.get().getInstanceId()).isEqualTo(instanceId);
        assertThat(provider.getRunningTaskCount()).isEqualTo(1);
    }

    @Test
    void shouldDenyTaskAcquisitionWhenTaskAlreadyRunning() {
        // Given
        String taskKey = "test-task";
        String instanceId1 = "instance-1";
        String instanceId2 = "instance-2";
        provider.tryAcquireTaskExecution(taskKey, instanceId1);

        // When
        Optional<TaskExecutionTicket> ticket = provider.tryAcquireTaskExecution(taskKey, instanceId2);

        // Then
        assertThat(ticket).isEmpty();
        assertThat(provider.getRunningTaskCount()).isEqualTo(1);
    }

    @Test
    void shouldDenyTaskAcquisitionWhenMaintenanceRequested() {
        // Given
        String taskKey = "test-task";
        String instanceId = "instance-1";
        provider.requestMaintenance("test-instance");

        // When
        Optional<TaskExecutionTicket> ticket = provider.tryAcquireTaskExecution(taskKey, instanceId);

        // Then
        assertThat(ticket).isEmpty();
        assertThat(provider.getStatus()).isEqualTo(CoordinationStatus.REQUESTED);
    }

    @Test
    void shouldDenyTaskAcquisitionWhenMaintenanceRunning() {
        // Given
        String taskKey = "test-task";
        String instanceId = "instance-1";
        provider.requestMaintenance("test-instance");
        provider.tryBeginMaintenance();

        // When
        Optional<TaskExecutionTicket> ticket = provider.tryAcquireTaskExecution(taskKey, instanceId);

        // Then
        assertThat(ticket).isEmpty();
        assertThat(provider.getStatus()).isEqualTo(CoordinationStatus.RUNNING);
    }

    @Test
    void shouldReleaseTaskAndDecrementCount() {
        // Given
        String taskKey = "test-task";
        String instanceId = "instance-1";
        Optional<TaskExecutionTicket> ticket = provider.tryAcquireTaskExecution(taskKey, instanceId);
        assertThat(ticket).isPresent();

        // When
        provider.releaseTaskExecution(ticket.get());

        // Then
        assertThat(provider.getRunningTaskCount()).isEqualTo(0);

        // Should be able to acquire again
        Optional<TaskExecutionTicket> newTicket = provider.tryAcquireTaskExecution(taskKey, instanceId);
        assertThat(newTicket).isPresent();
    }

    @Test
    void shouldTransitionToRequestedStatusWhenMaintenanceRequested() {
        // When
        boolean result = provider.requestMaintenance("test-instance");

        // Then
        assertThat(result).isTrue();
        assertThat(provider.getStatus()).isEqualTo(CoordinationStatus.REQUESTED);
    }

    @Test
    void shouldThrowExceptionWhenRequestingMaintenanceTwice() {
        // Given
        provider.requestMaintenance("test-instance");

        // When/Then
        assertThatThrownBy(() -> provider.requestMaintenance("test-instance"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Maintenance already requested or running");
    }

    @Test
    void shouldBeginMaintenanceWhenNoTasksRunning() {
        // Given
        provider.requestMaintenance("test-instance");

        // When
        boolean result = provider.tryBeginMaintenance();

        // Then
        assertThat(result).isTrue();
        assertThat(provider.getStatus()).isEqualTo(CoordinationStatus.RUNNING);
    }

    @Test
    void shouldNotBeginMaintenanceWhenTasksStillRunning() {
        // Given
        String taskKey = "test-task";
        String instanceId = "instance-1";
        provider.tryAcquireTaskExecution(taskKey, instanceId);
        provider.requestMaintenance("test-instance");

        // When
        boolean result = provider.tryBeginMaintenance();

        // Then
        assertThat(result).isFalse();
        assertThat(provider.getStatus()).isEqualTo(CoordinationStatus.REQUESTED);
    }

    @Test
    void shouldCompleteMaintenanceAndReturnToNormal() {
        // Given
        provider.requestMaintenance("test-instance");
        provider.tryBeginMaintenance();

        // When
        provider.completeMaintenance();

        // Then
        assertThat(provider.getStatus()).isEqualTo(CoordinationStatus.NORMAL);
    }

    @Test
    void shouldHandleMultipleConcurrentTasks() {
        // Given
        String task1 = "task-1";
        String task2 = "task-2";
        String task3 = "task-3";
        String instanceId = "instance-1";

        // When
        Optional<TaskExecutionTicket> ticket1 = provider.tryAcquireTaskExecution(task1, instanceId);
        Optional<TaskExecutionTicket> ticket2 = provider.tryAcquireTaskExecution(task2, instanceId);
        Optional<TaskExecutionTicket> ticket3 = provider.tryAcquireTaskExecution(task3, instanceId);

        // Then
        assertThat(ticket1).isPresent();
        assertThat(ticket2).isPresent();
        assertThat(ticket3).isPresent();
        assertThat(provider.getRunningTaskCount()).isEqualTo(3);

        // When - release one task
        provider.releaseTaskExecution(ticket2.get());

        // Then
        assertThat(provider.getRunningTaskCount()).isEqualTo(2);
    }

    @Test
    void shouldAllowMaintenanceAfterAllTasksComplete() {
        // Given
        String taskKey = "test-task";
        String instanceId = "instance-1";
        Optional<TaskExecutionTicket> ticket = provider.tryAcquireTaskExecution(taskKey, instanceId);
        assertThat(ticket).isPresent();

        // When - request maintenance while task is running
        provider.requestMaintenance("test-instance");
        boolean canBegin = provider.tryBeginMaintenance();
        assertThat(canBegin).isFalse();

        // When - release task and try again
        provider.releaseTaskExecution(ticket.get());
        boolean canBeginNow = provider.tryBeginMaintenance();

        // Then
        assertThat(canBeginNow).isTrue();
        assertThat(provider.getStatus()).isEqualTo(CoordinationStatus.RUNNING);
    }

    @Test
    void heartbeatShouldBeNoOp() {
        // When/Then - should not throw exception
        provider.heartbeat("instance-1");
    }

    @Test
    void cleanupStaleTasksShouldBeNoOp() {
        // When/Then - should not throw exception
        provider.cleanupStaleTasks();
    }
}
