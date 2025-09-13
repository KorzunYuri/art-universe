package yurykorzun.art.universe.music.quiz.service.step;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yurykorzun.art.universe.music.quiz.dto.GenerationStep;
import yurykorzun.art.universe.music.quiz.entity.GenerationStepType;
import yurykorzun.art.universe.music.quiz.repository.PipelineRepository;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FinalSelectionProcessorTest {

    @Mock
    private PipelineRepository pipelineRepository;

    @InjectMocks
    private FinalSelectionProcessor processor;

    @Test
    void validate_shouldPass_whenValidParams() {
        // given
        GenerationStep step = new GenerationStep();
        step.setType(GenerationStepType.FINAL_SELECTION);
        step.setParams(Map.of("targetCount", 20));

        // when & then
        assertDoesNotThrow(() -> processor.validate(step));
    }

    @Test
    void validate_shouldThrow_whenMissingTargetCount() {
        // given
        GenerationStep step = new GenerationStep();
        step.setType(GenerationStepType.FINAL_SELECTION);
        step.setParams(Map.of());

        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> processor.validate(step)
        );
        assertEquals("Final selection step requires 'targetCount' parameter", exception.getMessage());
    }

    @Test
    void validate_shouldThrow_whenTargetCountNotNumber() {
        // given
        GenerationStep step = new GenerationStep();
        step.setType(GenerationStepType.FINAL_SELECTION);
        step.setParams(Map.of("targetCount", "not_a_number"));

        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> processor.validate(step)
        );
        assertEquals("Parameter 'targetCount' must be a number", exception.getMessage());
    }

    @Test
    void process_shouldCallRepositoryWithCorrectParams() {
        // given
        GenerationStep step = new GenerationStep();
        step.setType(GenerationStepType.FINAL_SELECTION);
        step.setParams(Map.of("targetCount", 25));

        when(pipelineRepository.finalSelection(anyString(), anyString(), anyLong(), anyLong(), anyInt(), anyInt()))
            .thenReturn("result_table");

        // when
        String result = processor.process("schema.table", 1L, 2L, 3, step);

        // then
        assertEquals("result_table", result);
        verify(pipelineRepository).finalSelection("schema", "table", 1L, 2L, 3, 25);
    }
}
