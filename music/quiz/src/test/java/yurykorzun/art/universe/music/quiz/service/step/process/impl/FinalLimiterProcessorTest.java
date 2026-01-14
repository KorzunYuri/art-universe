package yurykorzun.art.universe.music.quiz.service.step.process.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yurykorzun.art.universe.common.test.config.CommonTestConfig;
import yurykorzun.art.universe.music.quiz.dto.step.StepRunResult;
import yurykorzun.art.universe.music.quiz.entity.Step;
import yurykorzun.art.universe.music.quiz.entity.StepRun;
import yurykorzun.art.universe.music.quiz.entity.StepType;
import yurykorzun.art.universe.music.quiz.service.step.process.StepProcessorRegistry;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FinalLimiterProcessorTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private Query query;

    private FinalLimiterProcessor processor;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = CommonTestConfig.getObjectMapper();
        processor = new FinalLimiterProcessor(mock(StepProcessorRegistry.class), objectMapper);
        processor.setEntityManager(entityManager);
    }

    @Test
    void processStep_shouldThrowException_whenNullInputTable() {
        // given
        Step step = Step.builder().cfgData("{\"targetCount\":20}").build();
        StepRun stepRun = StepRun.builder().build();

        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> processor.processStep(step, null, "output.table", stepRun)
        );

        assertEquals("DB object name cannot be null or empty", exception.getMessage());
    }

    @Test
    void processStep_shouldThrowException_whenInvalidInputTableFormat() {
        // given
        Step step = Step.builder().cfgData("{\"targetCount\":20}").build();
        StepRun stepRun = StepRun.builder().build();

        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> processor.processStep(step, "invalid_table", "output.table", stepRun)
        );

        assertEquals("DB object name must be in format 'schemaName.table', got: 'invalid_table'", exception.getMessage());
    }

    @Test
    void processStep_shouldCallProcedure_whenValidInput() {
        // given
        Step step = Step.builder()
            .id(1L)
            .type(StepType.FINAL_LIMITER)
            .cfgData("{\"targetCount\":20}")
            .build();
        
        StepRun stepRun = StepRun.builder().id(1L).build();
        
        final String inputTableName = "input.table";
        final String stepTableNameBase = "output.table";
        
        when(entityManager.createNativeQuery(anyString())).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.getSingleResult()).thenReturn(null);

        // when
        StepRunResult result = processor.processStep(step, inputTableName, stepTableNameBase, stepRun);

        // then
        assertNotNull(result);
        assertEquals(stepTableNameBase + "_limiter", result.getOutputTableName());
        verify(entityManager).createNativeQuery(contains("p_quiz_gen_tracks_step_final_selection"));
        verify(query).setParameter("inputTable", inputTableName);
        verify(query).setParameter("outputTable", stepTableNameBase + "_limiter");
        verify(query).setParameter("targetCount", 20);
        verify(query).getSingleResult();
    }

    @Test
    void validateConfiguration_shouldPass_whenValidConfig() {
        // given
        String validConfig = "{\"targetCount\":20}";

        // when & then
        assertDoesNotThrow(() -> processor.validateConfiguration(validConfig));
    }

    @Test
    void validateConfiguration_shouldThrow_whenInvalidJson() {
        // given
        String invalidConfig = "invalid json";

        // when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> processor.validateConfiguration(invalidConfig));
        
        assertEquals("Invalid configuration for final limiter step", exception.getMessage());
    }

    @Test
    void getPreview_shouldReturnEmptyJson() {
        // given
        Step step = Step.builder().build();

        // when
        String result = processor.getPreview(step);

        // then
        assertEquals("{}", result);
    }

    @Test
    void verifyConfigurationIsActual_shouldReturnSameConfig_whenValid() {
        // given
        String validConfig = "{\"targetCount\":20}";

        // when
        String result = processor.verifyConfigurationIsActual(validConfig);

        // then
        assertEquals(validConfig, result);
    }

    @Test
    void isActualVersion_shouldReturnTrue() {
        // when
        boolean result = processor.isActualVersion("{\"targetCount\":20}");

        // then
        assertTrue(result);
    }

    @Test
    void migrateConfiguration_shouldReturnSameConfig() {
        // given
        String config = "{\"targetCount\":20}";

        // when
        String result = processor.migrateConfiguration(config);

        // then
        assertEquals(config, result);
    }
}
