package yurykorzun.art.universe.music.quiz.service.step.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yurykorzun.art.universe.music.data.raw.lastfm.common.config.CommonTestConfig;
import yurykorzun.art.universe.music.quiz.dto.step.StepRunResult;
import yurykorzun.art.universe.music.quiz.dto.step.config.StartDatasourceStepConfig;
import yurykorzun.art.universe.music.quiz.entity.Step;
import yurykorzun.art.universe.music.quiz.entity.StepRun;
import yurykorzun.art.universe.music.quiz.entity.StepType;
import yurykorzun.art.universe.music.quiz.service.step.StepProcessorRegistry;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class StartDatasourceProcessorTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private Query query;

    private StartDatasourceProcessor processor;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = CommonTestConfig.getObjectMapper();
        processor = new StartDatasourceProcessor(mock(StepProcessorRegistry.class), objectMapper);
        processor.setEntityManager(entityManager);
    }

    @Test
    void processStep_shouldReturnDatasourceFromConfig_whenValidConfig() {
        // given
        Step step = Step.builder()
            .id(1L)
            .type(StepType.START_DATASOURCE)
            .cfgData("{}")
            .build();
        
        StepRun stepRun = StepRun.builder().id(1L).build();

        final String inputTableName = null;
        final String stepTableNameBase = "output_table";

        // when
        StepRunResult result = processor.processStep(step, inputTableName, stepTableNameBase, stepRun);

        // then
        assertNotNull(result);
        assertEquals(StartDatasourceStepConfig.DEFAULT_DATASOURCE, result.getOutputTableName());
    }

    @Test
    void validateConfiguration_shouldPass_whenValidConfig() {
        // given
        String validConfig = "{}";

        // when & then
        assertDoesNotThrow(() -> processor.validateConfiguration(validConfig));
    }

    @Test
    void validateConfiguration_shouldThrow_whenInvalidJson() {
        // given
        String invalidConfig = "invalid json";

        // when & then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> processor.validateConfiguration(invalidConfig));
        
        assertEquals("Failed to parse step configuration", exception.getMessage());
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
        String validConfig = "{}";

        // when
        String result = processor.verifyConfigurationIsActual(validConfig);

        // then
        assertEquals(validConfig, result);
    }

    @Test
    void isActualVersion_shouldReturnTrue() {
        // when
        boolean result = processor.isActualVersion("{}");

        // then
        assertTrue(result);
    }

    @Test
    void migrateConfiguration_shouldReturnSameConfig() {
        // given
        String config = "{}";

        // when
        String result = processor.migrateConfiguration(config);

        // then
        assertEquals(config, result);
    }
}
