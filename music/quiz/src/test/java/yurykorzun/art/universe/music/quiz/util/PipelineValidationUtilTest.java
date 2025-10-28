package yurykorzun.art.universe.music.quiz.util;

import org.junit.jupiter.api.Test;
import yurykorzun.art.universe.music.quiz.entity.Step;
import yurykorzun.art.universe.music.quiz.entity.StepType;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PipelineValidationUtilTest {

    // ========== validateStepPosition tests (for addStep) ==========
    
    @Test
    void validateStepPosition_shouldAllowStartStepAtFirstPosition() {
        List<StepType> existingSteps = List.of();
        
        assertDoesNotThrow(() -> 
            PipelineValidationUtil.validateStepPosition(StepType.START_DATASOURCE, 0, existingSteps));
    }

    @Test
    void validateStepPosition_shouldRejectStartStepNotAtFirstPosition() {
        List<StepType> existingSteps = List.of(StepType.APPROVED_FILTER);
        
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            PipelineValidationUtil.validateStepPosition(StepType.START_DATASOURCE, 1, existingSteps));
        assertEquals("START step must be at position 0", exception.getMessage());
    }

    @Test
    void validateStepPosition_shouldRejectMultipleStartSteps() {
        List<StepType> existingSteps = List.of(StepType.START_DATASOURCE);
        
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            PipelineValidationUtil.validateStepPosition(StepType.START_DATASOURCE, 0, existingSteps));
        assertEquals("Only one START step is allowed", exception.getMessage());
    }

    @Test
    void validateStepPosition_shouldAllowFinalStepAtLastPosition() {
        List<StepType> existingSteps = List.of(StepType.START_DATASOURCE, StepType.APPROVED_FILTER);
        
        assertDoesNotThrow(() -> 
            PipelineValidationUtil.validateStepPosition(StepType.FINAL_LIMITER, 2, existingSteps));
    }

    @Test
    void validateStepPosition_shouldRejectFinalStepNotAtLastPosition() {
        List<StepType> existingSteps = List.of(StepType.START_DATASOURCE, StepType.APPROVED_FILTER);
        
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            PipelineValidationUtil.validateStepPosition(StepType.FINAL_LIMITER, 1, existingSteps));
        assertEquals("FINAL step must be at last position", exception.getMessage());
    }

    @Test
    void validateStepPosition_shouldRejectMultipleFinalSteps() {
        List<StepType> existingSteps = List.of(StepType.START_DATASOURCE, StepType.FINAL_LIMITER);
        
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            PipelineValidationUtil.validateStepPosition(StepType.FINAL_CATEGORIES_BALANCER, 2, existingSteps));
        assertEquals("Only one FINAL step is allowed", exception.getMessage());
    }

    @Test
    void validateStepPosition_shouldAllowMiddleStepBetweenStartAndFinal() {
        List<StepType> existingSteps = List.of(StepType.START_DATASOURCE, StepType.FINAL_LIMITER);
        
        assertDoesNotThrow(() -> 
            PipelineValidationUtil.validateStepPosition(StepType.APPROVED_FILTER, 1, existingSteps));
    }

    @Test
    void validateStepPosition_shouldRejectMiddleStepWithStartAfter() {
        List<StepType> existingSteps = List.of(StepType.START_DATASOURCE);
        
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            PipelineValidationUtil.validateStepPosition(StepType.BLACKLIST_FILTER, 0, existingSteps));
        assertEquals("MIDDLE step cannot have START steps after it", exception.getMessage());
    }

    @Test
    void validateStepPosition_shouldRejectMiddleStepWithFinalBefore() {
        List<StepType> existingSteps = List.of(StepType.FINAL_LIMITER);
        
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            PipelineValidationUtil.validateStepPosition(StepType.APPROVED_FILTER, 1, existingSteps));
        assertEquals("MIDDLE step cannot have FINAL steps before it", exception.getMessage());
    }

    // ========== validateStepPositionForMove tests (for moveStep) ==========
    
    @Test
    void validateStepPositionForMove_shouldAllowMovingFinalStepToLastPosition() {
        // Pipeline: [FINAL_LIMITER, START_DATASOURCE] -> move FINAL_LIMITER to pos 1
        List<StepType> stepTypes = List.of(StepType.FINAL_LIMITER, StepType.START_DATASOURCE);
        
        assertDoesNotThrow(() -> 
            PipelineValidationUtil.validateStepPositionForMove(StepType.FINAL_LIMITER, 0, 1, stepTypes)
        );
    }

    @Test
    void validateStepPositionForMove_shouldRejectMovingFinalStepToMiddlePosition() {
        // Pipeline: [START_DATASOURCE, FINAL_LIMITER, APPROVED_FILTER] -> move FINAL_LIMITER to pos 1 (middle)
        List<StepType> stepTypes = List.of(StepType.START_DATASOURCE, StepType.FINAL_LIMITER, StepType.APPROVED_FILTER);
        
        assertThrows(IllegalArgumentException.class, () -> 
            PipelineValidationUtil.validateStepPositionForMove(StepType.FINAL_LIMITER, 1, 1, stepTypes)
        );
    }

    @Test
    void validateStepPositionForMove_shouldAllowMovingStartStepToFirstPosition() {
        // Pipeline: [FINAL_LIMITER, START_DATASOURCE] -> move START_DATASOURCE to pos 0
        List<StepType> stepTypes = List.of(StepType.FINAL_LIMITER, StepType.START_DATASOURCE);
        
        assertDoesNotThrow(() -> 
            PipelineValidationUtil.validateStepPositionForMove(StepType.START_DATASOURCE, 1, 0, stepTypes)
        );
    }

    @Test
    void validateStepPositionForMove_shouldRejectMovingStartStepToNonFirstPosition() {
        // Pipeline: [START_DATASOURCE, APPROVED_FILTER] -> move START_DATASOURCE to pos 1
        List<StepType> stepTypes = List.of(StepType.START_DATASOURCE, StepType.APPROVED_FILTER);
        
        assertThrows(IllegalArgumentException.class, () -> 
            PipelineValidationUtil.validateStepPositionForMove(StepType.START_DATASOURCE, 0, 1, stepTypes)
        );
    }

    @Test
    void validateStepPositionForMove_shouldHandleMultipleStepsOfSameType() {
        // Pipeline: [START_DATASOURCE, APPROVED_FILTER, APPROVED_FILTER, FINAL_LIMITER]
        // Move second APPROVED_FILTER from pos 2 to pos 1
        List<StepType> stepTypes = List.of(StepType.START_DATASOURCE, StepType.APPROVED_FILTER, StepType.APPROVED_FILTER, StepType.FINAL_LIMITER);
        
        assertDoesNotThrow(() -> 
            PipelineValidationUtil.validateStepPositionForMove(StepType.APPROVED_FILTER, 2, 1, stepTypes)
        );
    }

    @Test
    void validateStepPositionForMove_shouldAllowMovingMiddleStepWithinValidRange() {
        // Pipeline: [START_DATASOURCE, APPROVED_FILTER, BLACKLIST_FILTER, FINAL_LIMITER]
        // Move BLACKLIST_FILTER from pos 2 to pos 1
        List<StepType> stepTypes = List.of(StepType.START_DATASOURCE, StepType.APPROVED_FILTER, StepType.BLACKLIST_FILTER, StepType.FINAL_LIMITER);
        
        assertDoesNotThrow(() -> 
            PipelineValidationUtil.validateStepPositionForMove(StepType.BLACKLIST_FILTER, 2, 1, stepTypes)
        );
    }

    // ========== validatePipelineForGeneration tests ==========
    
    @Test
    void validatePipelineForGeneration_shouldAllowValidPipeline() {
        List<Step> steps = List.of(
            Step.builder().type(StepType.START_DATASOURCE).build(),
            Step.builder().type(StepType.APPROVED_FILTER).build(),
            Step.builder().type(StepType.FINAL_LIMITER).build()
        );
        
        assertDoesNotThrow(() -> 
            PipelineValidationUtil.validatePipelineForGeneration(steps));
    }

    @Test
    void validatePipelineForGeneration_shouldRejectEmptyPipeline() {
        List<Step> steps = List.of();
        
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            PipelineValidationUtil.validatePipelineForGeneration(steps));
        assertEquals("Pipeline must have at least one step", exception.getMessage());
    }

    @Test
    void validatePipelineForGeneration_shouldRejectPipelineWithoutStart() {
        List<Step> steps = List.of(
            Step.builder().type(StepType.APPROVED_FILTER).build(),
            Step.builder().type(StepType.FINAL_LIMITER).build()
        );
        
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            PipelineValidationUtil.validatePipelineForGeneration(steps));
        assertEquals("Pipeline must have exactly one START step", exception.getMessage());
    }

    @Test
    void validatePipelineForGeneration_shouldRejectPipelineWithoutFinal() {
        List<Step> steps = List.of(
            Step.builder().type(StepType.START_DATASOURCE).build(),
            Step.builder().type(StepType.APPROVED_FILTER).build()
        );
        
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            PipelineValidationUtil.validatePipelineForGeneration(steps));
        assertEquals("Pipeline must have exactly one FINAL step", exception.getMessage());
    }

    @Test
    void validatePipelineForGeneration_shouldRejectPipelineWithMultipleStarts() {
        List<Step> steps = List.of(
            Step.builder().type(StepType.START_DATASOURCE).build(),
            Step.builder().type(StepType.START_DATASOURCE).build(),
            Step.builder().type(StepType.FINAL_LIMITER).build()
        );
        
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            PipelineValidationUtil.validatePipelineForGeneration(steps));
        assertEquals("Pipeline must have exactly one START step", exception.getMessage());
    }

    @Test
    void validatePipelineForGeneration_shouldRejectPipelineWithMultipleFinals() {
        List<Step> steps = List.of(
            Step.builder().type(StepType.START_DATASOURCE).build(),
            Step.builder().type(StepType.FINAL_LIMITER).build(),
            Step.builder().type(StepType.FINAL_CATEGORIES_BALANCER).build()
        );
        
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            PipelineValidationUtil.validatePipelineForGeneration(steps));
        assertEquals("Pipeline must have exactly one FINAL step", exception.getMessage());
    }
}
