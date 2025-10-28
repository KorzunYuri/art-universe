package yurykorzun.art.universe.music.quiz.util;

import yurykorzun.art.universe.music.quiz.entity.Step;
import yurykorzun.art.universe.music.quiz.entity.StepPosition;
import yurykorzun.art.universe.music.quiz.entity.StepType;

import java.util.List;

public class PipelineValidationUtil {

    public static void validateStepPosition(StepType stepType, int targetPosition, List<StepType> existingStepTypesSequence) {
        validateStepPosition(targetPosition, existingStepTypesSequence.size());
        List<StepType> stepTypesAfterChange = getStepTypesAfterAdd(stepType, targetPosition, existingStepTypesSequence);
        validateStepPositions(stepType, targetPosition, stepTypesAfterChange);
    }

    public static void validateStepPositionForMove(StepType stepType, int currentPosition, int targetPosition, List<StepType> existingStepTypesSequence) {
        validateStepPosition(targetPosition, existingStepTypesSequence.size());
        List<StepType> stepTypesAfterMove = getStepTypesAfterMove(currentPosition, targetPosition, existingStepTypesSequence);
        validateStepPositions(stepType, targetPosition, stepTypesAfterMove);
    }

    private static void validateStepPosition(int targetPosition, int stepsCount) {
        if (targetPosition > stepsCount || targetPosition < 0) {
            throw new IllegalArgumentException(String.format("target step position %s is out of pipeline bounds %s", targetPosition, stepsCount));
        }
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
                if (position != 0) {
                    throw new IllegalArgumentException("START step must be at position 0");
                }
                long startCount = stepTypesAfterChange.stream()
                    .filter(type -> type.getPosition() == StepPosition.START)
                    .count();
                if (startCount > 1) {
                    throw new IllegalArgumentException("Only one START step is allowed");
                }
            }
            case FINAL -> {
                if (position != stepTypesAfterChange.size() - 1) {
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
                for (int i = position + 1; i < stepTypesAfterChange.size(); i++) {
                    if (stepTypesAfterChange.get(i).getPosition() == StepPosition.START) {
                        throw new IllegalArgumentException("MIDDLE step cannot have START steps after it");
                    }
                }
                // Check no FINAL steps before this position
                for (int i = 0; i < position; i++) {
                    if (stepTypesAfterChange.get(i).getPosition() == StepPosition.FINAL) {
                        throw new IllegalArgumentException("MIDDLE step cannot have FINAL steps before it");
                    }
                }
            }
        }
    }

    private static List<StepType> getStepTypesAfterAdd(StepType newStepType, int targetPosition, List<StepType> existingStepTypesSequence) {
        List<StepType> result = new java.util.ArrayList<>(existingStepTypesSequence);
        result.add(targetPosition, newStepType);
        return result;
    }

    private static List<StepType> getStepTypesAfterMove(int currentPosition, int newPosition, List<StepType> existingStepTypesSequence) {
        List<StepType> result = new java.util.ArrayList<>(existingStepTypesSequence);
        StepType movingStepType = result.remove(currentPosition);
        result.add(newPosition, movingStepType);
        return result;
    }
}
