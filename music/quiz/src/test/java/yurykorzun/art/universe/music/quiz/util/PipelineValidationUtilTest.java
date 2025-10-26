package yurykorzun.art.universe.music.quiz.util;

import org.junit.jupiter.api.Test;
import yurykorzun.art.universe.music.quiz.entity.Step;
import yurykorzun.art.universe.music.quiz.entity.StepType;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PipelineValidationUtilTest {

    @Test
    void validateStepPosition_shouldAllowStartStepAtFirstPosition() {
        // given
        List<StepType> existingSteps = List.of();
        
        // when & then
        assertDoesNotThrow(() -> 
            PipelineValidationUtil.validateStepPosition(StepType.START_DATASOURCE, 1, existingSteps));
    }

    @Test
    void validateStepPosition_shouldRejectStartStepNotAtFirstPosition() {
        // given
        List<StepType> existingSteps = List.of(StepType.APPROVED_FILTER);
        
        // when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            PipelineValidationUtil.validateStepPosition(StepType.START_DATASOURCE, 2, existingSteps));
        assertEquals("START step must be at position 1", exception.getMessage());
    }

    @Test
    void validateStepPosition_shouldRejectMultipleStartSteps() {
        // given
        List<StepType> existingSteps = List.of(StepType.START_DATASOURCE);
        
        // when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            PipelineValidationUtil.validateStepPosition(StepType.START_DATASOURCE, 1, existingSteps));
        assertEquals("Only one START step is allowed", exception.getMessage());
    }

    @Test
    void validateStepPosition_shouldAllowFinalStepAtLastPosition() {
        // given
        List<StepType> existingSteps = List.of(StepType.START_DATASOURCE, StepType.APPROVED_FILTER);
        
        // when & then
        assertDoesNotThrow(() -> 
            PipelineValidationUtil.validateStepPosition(StepType.FINAL_LIMITER, 3, existingSteps));
    }

    @Test
    void validateStepPosition_shouldRejectFinalStepNotAtLastPosition() {
        // given
        List<StepType> existingSteps = List.of(StepType.START_DATASOURCE, StepType.APPROVED_FILTER);
        
        // when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            PipelineValidationUtil.validateStepPosition(StepType.FINAL_LIMITER, 2, existingSteps));
        assertEquals("FINAL step must be at last position", exception.getMessage());
    }

    @Test
    void validateStepPosition_shouldRejectMultipleFinalSteps() {
        // given
        List<StepType> existingSteps = List.of(StepType.START_DATASOURCE, StepType.FINAL_LIMITER);
        
        // when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            PipelineValidationUtil.validateStepPosition(StepType.FINAL_CATEGORIES_BALANCER, 3, existingSteps));
        assertEquals("Only one FINAL step is allowed", exception.getMessage());
    }

    @Test
    void validateStepPosition_shouldAllowMiddleStepAnywhere() {
        // given
        List<StepType> existingSteps = List.of(StepType.START_DATASOURCE, StepType.FINAL_LIMITER);
        
        // when & then
        assertDoesNotThrow(() -> 
            PipelineValidationUtil.validateStepPosition(StepType.APPROVED_FILTER, 2, existingSteps));
    }

    @Test
    void validateStepPosition_shouldRejectMiddleStepWithStartAfter() {
        // given
        List<StepType> existingSteps = List.of(StepType.START_DATASOURCE);
        
        // when & then - trying to add MIDDLE step at position 1, which would put START after it
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            PipelineValidationUtil.validateStepPosition(StepType.BLACKLIST_FILTER, 1, existingSteps));
        assertEquals("MIDDLE step cannot have START steps after it", exception.getMessage());
    }

    @Test
    void validateStepPosition_shouldRejectMiddleStepWithFinalBefore() {
        // given
        List<StepType> existingSteps = List.of(StepType.FINAL_LIMITER);
        
        // when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            PipelineValidationUtil.validateStepPosition(StepType.APPROVED_FILTER, 2, existingSteps));
        assertEquals("MIDDLE step cannot have FINAL steps before it", exception.getMessage());
    }

    @Test
    void validateStepPositionForMove_shouldAllowValidMove() {
        // given
        List<StepType> allSteps = List.of(StepType.START_DATASOURCE, StepType.APPROVED_FILTER, StepType.FINAL_LIMITER);
        
        // when & then
        assertDoesNotThrow(() -> 
            PipelineValidationUtil.validateStepPositionForMove(StepType.APPROVED_FILTER, 2, allSteps));
    }

    @Test
    void validateStepPositionForMove_shouldRejectInvalidMove() {
        // given
        List<StepType> allSteps = List.of(StepType.START_DATASOURCE, StepType.APPROVED_FILTER, StepType.FINAL_LIMITER);
        
        // when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            PipelineValidationUtil.validateStepPositionForMove(StepType.FINAL_LIMITER, 2, allSteps));
        assertEquals("FINAL step must be at last position", exception.getMessage());
    }

    @Test
    void validatePipelineForGeneration_shouldAllowValidPipeline() {
        // given
        List<Step> steps = List.of(
            Step.builder().type(StepType.START_DATASOURCE).build(),
            Step.builder().type(StepType.APPROVED_FILTER).build(),
            Step.builder().type(StepType.FINAL_LIMITER).build()
        );
        
        // when & then
        assertDoesNotThrow(() -> 
            PipelineValidationUtil.validatePipelineForGeneration(steps));
    }

    @Test
    void validatePipelineForGeneration_shouldRejectEmptyPipeline() {
        // given
        List<Step> steps = List.of();
        
        // when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            PipelineValidationUtil.validatePipelineForGeneration(steps));
        assertEquals("Pipeline must have at least one step", exception.getMessage());
    }

    @Test
    void validatePipelineForGeneration_shouldRejectPipelineWithoutStart() {
        // given
        List<Step> steps = List.of(
            Step.builder().type(StepType.APPROVED_FILTER).build(),
            Step.builder().type(StepType.FINAL_LIMITER).build()
        );
        
        // when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            PipelineValidationUtil.validatePipelineForGeneration(steps));
        assertEquals("Pipeline must have exactly one START step", exception.getMessage());
    }

    @Test
    void validatePipelineForGeneration_shouldRejectPipelineWithoutFinal() {
        // given
        List<Step> steps = List.of(
            Step.builder().type(StepType.START_DATASOURCE).build(),
            Step.builder().type(StepType.APPROVED_FILTER).build()
        );
        
        // when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            PipelineValidationUtil.validatePipelineForGeneration(steps));
        assertEquals("Pipeline must have exactly one FINAL step", exception.getMessage());
    }

    @Test
    void validatePipelineForGeneration_shouldRejectPipelineWithMultipleStarts() {
        // given
        List<Step> steps = List.of(
            Step.builder().type(StepType.START_DATASOURCE).build(),
            Step.builder().type(StepType.START_DATASOURCE).build(),
            Step.builder().type(StepType.FINAL_LIMITER).build()
        );
        
        // when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            PipelineValidationUtil.validatePipelineForGeneration(steps));
        assertEquals("Pipeline must have exactly one START step", exception.getMessage());
    }

    @Test
    void validatePipelineForGeneration_shouldRejectPipelineWithMultipleFinals() {
        // given
        List<Step> steps = List.of(
            Step.builder().type(StepType.START_DATASOURCE).build(),
            Step.builder().type(StepType.FINAL_LIMITER).build(),
            Step.builder().type(StepType.FINAL_CATEGORIES_BALANCER).build()
        );
        
        // when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            PipelineValidationUtil.validatePipelineForGeneration(steps));
        assertEquals("Pipeline must have exactly one FINAL step", exception.getMessage());
    }
}
