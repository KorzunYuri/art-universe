package yurykorzun.art.universe.music.quiz.service.step.process;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import yurykorzun.art.universe.music.quiz.entity.StepType;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StepProcessorRegistryTest {

    private StepProcessorRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new StepProcessorRegistry();
    }

    @Test
    void register_shouldStoreProcessor() {
        // given
        StepProcessor processor = mock(StepProcessor.class);
        when(processor.getStepType()).thenReturn(StepType.APPROVED_FILTER);

        // when
        registry.register(processor);

        // then
        StepProcessor result = registry.get(StepType.APPROVED_FILTER);
        assertSame(processor, result);
    }

    @Test
    void register_shouldOverwriteExistingProcessor_andLogWarning() {
        // given
        StepProcessor first = mock(StepProcessor.class);
        StepProcessor second = mock(StepProcessor.class);
        when(first.getStepType()).thenReturn(StepType.BLACKLIST_FILTER);
        when(second.getStepType()).thenReturn(StepType.BLACKLIST_FILTER);

        // when
        registry.register(first);
        registry.register(second);

        // then
        StepProcessor result = registry.get(StepType.BLACKLIST_FILTER);
        assertSame(second, result);
    }

    @Test
    void get_shouldThrowException_whenProcessorNotFound() {
        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> registry.get(StepType.FINAL_LIMITER)
        );

        assertEquals("No processor found for step type: FINAL_LIMITER", exception.getMessage());
    }
}
