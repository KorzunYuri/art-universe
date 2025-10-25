package yurykorzun.art.universe.music.quiz.util;

import yurykorzun.art.universe.music.quiz.entity.Step;
import yurykorzun.art.universe.music.quiz.entity.StepPosition;
import yurykorzun.art.universe.music.quiz.entity.StepType;

import java.util.List;

public class PipelineValidationUtil {

    public static void validateStepPosition(StepType stepType, int targetPosition, List<StepType> existingStepTypes) {
        List<StepType> stepTypesAfterChange = getStepTypesAfterAdd(stepType, targetPosition, existingStepTypes);
        validateStepPositions(stepType, targetPosition, stepTypesAfterChange);
    }

    public static void validateStepPositionForMove(StepType stepType, int newPosition, List<StepType> allStepTypes) {
        List<StepType> stepTypesAfterMove = getStepTypesAfterMove(stepType, newPosition, allStepTypes);
        validateStepPositions(stepType, newPosition, stepTypesAfterMove);
    }

    public static void validatePipelineForGeneration(List<Step> steps) {
        if (steps.isEmpty()) {
            throw new IllegalArgumentException("Pipeline must have at least one step");
        }

        long startSteps = steps.stream()
            .filter(step -> step.getType().getPosition() == StepPosition.START)
            .count();

        long finalSteps = steps.stream()
            .filter(step -> step.getType().getPosition() == StepPosition.FINAL)
            .count();

        if (startSteps != 1) {
            throw new IllegalArgumentException("Pipeline must have exactly one START step");
        }

        if (finalSteps != 1) {
            throw new IllegalArgumentException("Pipeline must have exactly one FINAL step");
        }
    }

    private static void validateStepPositions(StepType stepType, int position, List<StepType> stepTypesAfterChange) {
        StepPosition stepPosition = stepType.getPosition();

        switch (stepPosition) {
            case START -> {
                if (position != 1) {
                    throw new IllegalArgumentException("START step must be at position 1");
                }
                long startCount = stepTypesAfterChange.stream()
                    .filter(type -> type.getPosition() == StepPosition.START)
                    .count();
                if (startCount > 1) {
                    throw new IllegalArgumentException("Only one START step is allowed");
                }
            }
            case FINAL -> {
                if (position != stepTypesAfterChange.size()) {
                    throw new IllegalArgumentException("FINAL step must be at last position");
                }
                long finalCount = stepTypesAfterChange.stream()
                    .filter(type -> type.getPosition() == StepPosition.FINAL)
                    .count();
                if (finalCount > 1) {
                    throw new IllegalArgumentException("Only one FINAL step is allowed");
                }
            }
            case MIDDLE -> {
                // Check no START steps after this position
                for (int i = position; i < stepTypesAfterChange.size(); i++) {
                    if (stepTypesAfterChange.get(i).getPosition() == StepPosition.START) {
                        throw new IllegalArgumentException("MIDDLE step cannot have START steps after it");
                    }
                }
                // Check no FINAL steps before this position
                for (int i = 0; i < position - 1; i++) {
                    if (stepTypesAfterChange.get(i).getPosition() == StepPosition.FINAL) {
                        throw new IllegalArgumentException("MIDDLE step cannot have FINAL steps before it");
                    }
                }
            }
        }
    }

    private static List<StepType> getStepTypesAfterAdd(StepType newStepType, int targetPosition, List<StepType> existingStepTypes) {
        List<StepType> result = new java.util.ArrayList<>(existingStepTypes);
        result.add(targetPosition - 1, newStepType);
        return result;
    }

    private static List<StepType> getStepTypesAfterMove(StepType movingStepType, int newPosition, List<StepType> allStepTypes) {
        List<StepType> result = new java.util.ArrayList<>(allStepTypes);
        result.remove(movingStepType);
        result.add(newPosition - 1, movingStepType);
        return result;
    }
}
