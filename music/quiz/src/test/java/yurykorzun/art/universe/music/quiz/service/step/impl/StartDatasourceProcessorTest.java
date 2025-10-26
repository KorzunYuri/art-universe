package yurykorzun.art.universe.music.quiz.service.step.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import yurykorzun.art.universe.music.quiz.dto.step.StepRunResult;
import yurykorzun.art.universe.music.quiz.dto.step.config.StartDatasourceStepConfig;
import yurykorzun.art.universe.music.quiz.entity.Step;
import yurykorzun.art.universe.music.quiz.entity.StepRun;
import yurykorzun.art.universe.music.quiz.entity.StepType;
import yurykorzun.art.universe.music.quiz.repository.StepRepository;
import yurykorzun.art.universe.music.quiz.repository.StepRunRepository;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class StartDatasourceProcessorTest {

    @Mock
    private StepRunRepository stepRunRepository;

    @Mock
    private StepRepository stepRepository;

    @InjectMocks
    private StartDatasourceProcessor processor;

    @Test
    void processStep_shouldReturnDatasourceFromConfig_whenValidConfig() {
        // given
        ObjectMapper objectMapper = new ObjectMapper();
        processor = new StartDatasourceProcessor(stepRunRepository, stepRepository, objectMapper);
        
        Step step = Step.builder()
            .id(1L)
            .type(StepType.START_DATASOURCE)
            .cfgData("{}")
            .build();
        
        StepRun stepRun = StepRun.builder().id(1L).build();

        // when
        StepRunResult result = ReflectionTestUtils.invokeMethod(processor, "processStep", step, null, "output_table", stepRun);

        // then
        assertNotNull(result);
        assertEquals(StartDatasourceStepConfig.DEFAULT_DATASOURCE, result.getOutputTableName());
    }

    @Test
    void validateConfiguration_shouldPass_whenValidConfig() {
        // given
        ObjectMapper objectMapper = new ObjectMapper();
        processor = new StartDatasourceProcessor(stepRunRepository, stepRepository, objectMapper);
        
        String validConfig = "{}";

        // when & then
        assertDoesNotThrow(() -> processor.validateConfiguration(validConfig));
    }

    @Test
    void validateConfiguration_shouldThrow_whenInvalidJson() {
        // given
        ObjectMapper objectMapper = new ObjectMapper();
        processor = new StartDatasourceProcessor(stepRunRepository, stepRepository, objectMapper);
        
        String invalidConfig = "invalid json";

        // when & then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> processor.validateConfiguration(invalidConfig));
        
        assertEquals("Failed to parse step configuration", exception.getMessage());
    }
}
