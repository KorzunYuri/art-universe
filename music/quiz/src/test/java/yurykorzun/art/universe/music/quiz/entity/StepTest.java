package yurykorzun.art.universe.music.quiz.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StepTest {

    @Test
    void builder_shouldCreateStepWithAllFields() {
        // when
        Step step = Step.builder()
            .type(StepType.START_DATASOURCE)
            .algVersion(1)
            .cfgData("{}")
            .previewData("{\"preview\": \"data\"}")
            .lastStepRunId(100L)
            .deleted(false)
            .immutable(true)
            .build();

        // then
        assertNotNull(step);
        assertEquals(StepType.START_DATASOURCE, step.getType());
        assertEquals(1, step.getAlgVersion());
        assertEquals("{}", step.getCfgData());
        assertEquals("{\"preview\": \"data\"}", step.getPreviewData());
        assertEquals(100L, step.getLastStepRunId());
        assertFalse(step.getDeleted());
        assertTrue(step.getImmutable());
        assertNotNull(step.getCreatedAt());
        assertNotNull(step.getUpdatedAt());
    }

    @Test
    void builder_shouldCreateStepWithDefaults() {
        // when
        Step step = Step.builder()
            .type(StepType.FINAL_LIMITER)
            .algVersion(1)
            .build();

        // then
        assertNotNull(step);
        assertEquals(StepType.FINAL_LIMITER, step.getType());
        assertEquals(1, step.getAlgVersion());
        assertFalse(step.getDeleted());
        assertFalse(step.getImmutable());
    }
}
