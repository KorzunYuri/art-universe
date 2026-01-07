package yurykorzun.art.universe.music.data.raw.lastfm.maintenance.provider;

import org.junit.jupiter.api.BeforeEach;
import yurykorzun.art.universe.music.data.raw.lastfm.coordination.entity.CoordinationStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yurykorzun.art.universe.music.data.raw.lastfm.coordination.entity.CoordinationStateEntity;
import yurykorzun.art.universe.music.data.raw.lastfm.coordination.repository.CoordinationInstanceRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.coordination.repository.CoordinationRunningTaskRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.coordination.repository.CoordinationStateRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.maintenance.coordination.provider.DatabaseCoordinationProvider;
import yurykorzun.art.universe.music.data.raw.lastfm.maintenance.coordination.TaskExecutionTicket;
import yurykorzun.art.universe.music.data.raw.lastfm.maintenance.coordination.TaskExecutionTicketImpl;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DatabaseCoordinationProviderTest {

    @Mock
    private CoordinationStateRepository stateRepository;

    @Mock
    private CoordinationInstanceRepository instanceRepository;

    @Mock
    private CoordinationRunningTaskRepository runningTaskRepository;

    private DatabaseCoordinationProvider provider;
    private final String instanceId = "test-module-host-123-456";
    private final int staleTimeoutSeconds = 120;

    @BeforeEach
    void setUp() {
        provider = new DatabaseCoordinationProvider(
                stateRepository,
                instanceRepository,
                runningTaskRepository,
                instanceId,
                staleTimeoutSeconds
        );
    }

    @Test
    void shouldAcquireTaskWhenStatusIsNormalAndTaskNotRunning() {
        // Given
        String taskKey = "test-task";
        CoordinationStateEntity state = CoordinationStateEntity.builder()
                .id(1)
                .status(CoordinationStatus.NORMAL)
                .updatedAt(Instant.now())
                .build();

        when(stateRepository.findById(1)).thenReturn(Optional.of(state));
        when(runningTaskRepository.tryInsertTask(eq(taskKey), eq(instanceId), any(Instant.class)))
                .thenReturn(1);

        // When
        Optional<TaskExecutionTicket> ticket = provider.tryAcquireTaskExecution(taskKey, instanceId);

        // Then
        assertThat(ticket).isPresent();
        assertThat(ticket.get().getTaskKey()).isEqualTo(taskKey);
        assertThat(ticket.get().getInstanceId()).isEqualTo(instanceId);
        verify(runningTaskRepository).tryInsertTask(eq(taskKey), eq(instanceId), any(Instant.class));
    }

    @Test
    void shouldDenyTaskAcquisitionWhenStatusIsRequested() {
        // Given
        String taskKey = "test-task";
        CoordinationStateEntity state = CoordinationStateEntity.builder()
                .id(1)
                .status(CoordinationStatus.REQUESTED)
                .updatedAt(Instant.now())
                .build();

        when(stateRepository.findById(1)).thenReturn(Optional.of(state));

        // When
        Optional<TaskExecutionTicket> ticket = provider.tryAcquireTaskExecution(taskKey, instanceId);

        // Then
        assertThat(ticket).isEmpty();
        verify(runningTaskRepository, never()).tryInsertTask(anyString(), anyString(), any(Instant.class));
    }

    @Test
    void shouldDenyTaskAcquisitionWhenTaskAlreadyRunning() {
        // Given
        String taskKey = "test-task";
        CoordinationStateEntity state = CoordinationStateEntity.builder()
                .id(1)
                .status(CoordinationStatus.NORMAL)
                .updatedAt(Instant.now())
                .build();

        when(stateRepository.findById(1)).thenReturn(Optional.of(state));
        when(runningTaskRepository.tryInsertTask(eq(taskKey), eq(instanceId), any(Instant.class)))
                .thenReturn(0); // Conflict - task already running

        // When
        Optional<TaskExecutionTicket> ticket = provider.tryAcquireTaskExecution(taskKey, instanceId);

        // Then
        assertThat(ticket).isEmpty();
    }

    @Test
    void shouldReleaseTask() {
        // Given
        String taskKey = "test-task";
        TaskExecutionTicket ticket = new TaskExecutionTicketImpl(taskKey, instanceId, Instant.now());

        // When
        provider.releaseTaskExecution(ticket);

        // Then
        verify(runningTaskRepository).deleteById(taskKey);
    }

    @Test
    void shouldReturnRunningTaskCount() {
        // Given
        when(runningTaskRepository.count()).thenReturn(5L);

        // When
        int count = provider.getRunningTaskCount();

        // Then
        assertThat(count).isEqualTo(5);
    }

    @Test
    void shouldRequestMaintenanceWhenStatusIsNormal() {
        // Given
        CoordinationStateEntity state = CoordinationStateEntity.builder()
                .id(1)
                .status(CoordinationStatus.NORMAL)
                .updatedAt(Instant.now())
                .build();

        when(stateRepository.findById(1)).thenReturn(Optional.of(state));

        // When
        boolean result = provider.requestMaintenance("test-instance");

        // Then
        assertThat(result).isTrue();
        verify(stateRepository).save(argThat(entity ->
                CoordinationStatus.REQUESTED.equals(entity.getStatus()) &&
                        "test-instance".equals(entity.getUpdatedByInstance())
        ));
    }

    @Test
    void shouldThrowExceptionWhenRequestingMaintenanceWhileAlreadyRequested() {
        // Given
        CoordinationStateEntity state = CoordinationStateEntity.builder()
                .id(1)
                .status(CoordinationStatus.REQUESTED)
                .updatedAt(Instant.now())
                .build();

        when(stateRepository.findById(1)).thenReturn(Optional.of(state));

        // When/Then
        assertThatThrownBy(() -> provider.requestMaintenance("test-instance"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Maintenance already requested or running");
    }

    @Test
    void shouldBeginMaintenanceWhenNoTasksRunningAndStatusIsRequested() {
        // Given
        CoordinationStateEntity state = CoordinationStateEntity.builder()
                .id(1)
                .status(CoordinationStatus.REQUESTED)
                .updatedAt(Instant.now())
                .build();

        when(runningTaskRepository.count()).thenReturn(0L);
        when(stateRepository.findById(1)).thenReturn(Optional.of(state));

        // When
        boolean result = provider.tryBeginMaintenance();

        // Then
        assertThat(result).isTrue();
        verify(stateRepository).save(argThat(entity ->
                CoordinationStatus.RUNNING.equals(entity.getStatus()) &&
                        instanceId.equals(entity.getUpdatedByInstance())
        ));
    }

    @Test
    void shouldNotBeginMaintenanceWhenTasksStillRunning() {
        // Given
        when(runningTaskRepository.count()).thenReturn(3L);

        // When
        boolean result = provider.tryBeginMaintenance();

        // Then
        assertThat(result).isFalse();
        verify(stateRepository, never()).save(any());
    }

    @Test
    void shouldNotBeginMaintenanceWhenStatusIsNotRequested() {
        // Given
        CoordinationStateEntity state = CoordinationStateEntity.builder()
                .id(1)
                .status(CoordinationStatus.NORMAL)
                .updatedAt(Instant.now())
                .build();

        when(runningTaskRepository.count()).thenReturn(0L);
        when(stateRepository.findById(1)).thenReturn(Optional.of(state));

        // When
        boolean result = provider.tryBeginMaintenance();

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void shouldCompleteMaintenance() {
        // Given
        CoordinationStateEntity state = CoordinationStateEntity.builder()
                .id(1)
                .status(CoordinationStatus.RUNNING)
                .updatedAt(Instant.now())
                .build();

        when(stateRepository.findById(1)).thenReturn(Optional.of(state));

        // When
        provider.completeMaintenance();

        // Then
        verify(stateRepository).save(argThat(entity ->
                CoordinationStatus.NORMAL.equals(entity.getStatus()) &&
                        instanceId.equals(entity.getUpdatedByInstance())
        ));
    }

    @Test
    void shouldReturnCorrectStatus() {
        // Given
        CoordinationStateEntity state = CoordinationStateEntity.builder()
                .id(1)
                .status(CoordinationStatus.REQUESTED)
                .updatedAt(Instant.now())
                .build();

        when(stateRepository.findById(1)).thenReturn(Optional.of(state));

        // When
        CoordinationStatus status = provider.getStatus();

        // Then
        assertThat(status).isEqualTo(CoordinationStatus.REQUESTED);
    }

    @Test
    void shouldUpdateHeartbeat() {
        // Given
        when(instanceRepository.updateHeartbeat(eq(instanceId), any(Instant.class)))
                .thenReturn(1);

        // When
        provider.heartbeat(instanceId);

        // Then
        verify(instanceRepository).updateHeartbeat(eq(instanceId), any(Instant.class));
    }

    @Test
    void shouldCleanupStaleInstances() {
        // Given
        List<String> staleInstances = Arrays.asList("instance-1", "instance-2");
        CoordinationStateEntity state = CoordinationStateEntity.builder()
                .id(1)
                .status(CoordinationStatus.NORMAL)
                .updatedAt(Instant.now())
                .build();

        when(instanceRepository.findStaleInstances(any(Instant.class)))
                .thenReturn(staleInstances);
        when(stateRepository.findById(1)).thenReturn(Optional.of(state));

        // When
        provider.cleanupStaleTasks();

        // Then
        verify(instanceRepository).findStaleInstances(any(Instant.class));
        verify(instanceRepository).deleteAllById(staleInstances);
    }

    @Test
    void shouldNotDeleteWhenNoStaleInstances() {
        // Given
        when(instanceRepository.findStaleInstances(any(Instant.class)))
                .thenReturn(List.of());

        // When
        provider.cleanupStaleTasks();

        // Then
        verify(instanceRepository).findStaleInstances(any(Instant.class));
        verify(instanceRepository, never()).deleteAllById(anyList());
    }

    @Test
    void shouldResetStateWhenStaleInstanceWasRequestingMaintenance() {
        // Given
        List<String> staleInstances = Arrays.asList("instance-1", "instance-2");
        CoordinationStateEntity state = CoordinationStateEntity.builder()
                .id(1)
                .status(CoordinationStatus.REQUESTED)
                .updatedAt(Instant.now())
                .updatedByInstance("instance-1")  // Stale instance was requesting maintenance
                .build();

        when(instanceRepository.findStaleInstances(any(Instant.class)))
                .thenReturn(staleInstances);
        when(stateRepository.findById(1)).thenReturn(Optional.of(state));

        // When
        provider.cleanupStaleTasks();

        // Then
        verify(stateRepository).save(argThat(entity ->
                CoordinationStatus.NORMAL.equals(entity.getStatus()) &&
                        entity.getUpdatedByInstance() == null
        ));
        verify(instanceRepository).deleteAllById(staleInstances);
    }
}
