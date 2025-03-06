package yurykorzun.art.universe.common.data.raw.task.entity;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

class TaskTest {

    private static final Supplier<Task> validTaskSupplier = () -> Task.builder()
            .type(TestTaskType.TEST_TASK)
            .dueDttm(Instant.now())
        .build();

    @Test
    void testValidTaskInitialState() {
        Task task = validTaskSupplier.get();
        assertEquals(TaskStatus.CREATED, task.getStatus());
        assertEquals(0, task.getAttemptCnt());
    }

    @Test
    void testValidTaskStatusTransition() {
        Task task = validTaskSupplier.get();
        task.setStatus(TaskStatus.PROCESSING);
        assertEquals(TaskStatus.PROCESSING, task.getStatus());
    }

    @Test
    void testInvalidTaskStatusTransition() {
        Task task = validTaskSupplier.get();
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> task.setStatus(TaskStatus.CREATED));
        assertEquals("Invalid transition from CREATED to CREATED", e.getMessage());
    }

    @Test
    void testAttemptsIncrement() {
        Task task = validTaskSupplier.get();
        task.incAttempts();
        assertEquals(1, task.getAttemptCnt());
    }

    @Test
    void testIncompleteTaskException() {
        List<Supplier<Task>> incompleteTaskSuppliers = List.of(
                () -> Task.builder()
                        .dueDttm(Instant.now())
                    .build(),
                () -> Task.builder()
                        .type(TestTaskType.TEST_TASK)
                    .build()
        );

        incompleteTaskSuppliers.forEach(s -> {
            Exception e = assertThrows(Exception.class, s::get);
            assertInstanceOf(NullPointerException.class, e);
            assertTrue(e.getMessage().endsWith(" is marked non-null but is null"));
        });
    }

}