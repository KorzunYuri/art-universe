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

        long initialSteps = steps.stream()
            .filter(step -> step.getType().getPosition() == StepPosition.INITIAL)
            .count();

        if (initialSteps != 1) {
            throw new IllegalArgumentException("Pipeline must have exactly one INITIAL step");
        }
    }

    private static void validateStepPositions(StepType stepType, int position, List<StepType> stepTypesAfterChange) {
        StepPosition stepPosition = stepType.getPosition();

        switch (stepPosition) {
            case INITIAL -> {
                if (position != 0) {
                    throw new IllegalArgumentException("INITIAL step must be at position 0");
                }
                long initialCount = stepTypesAfterChange.stream()
                    .filter(type -> type.getPosition() == StepPosition.INITIAL)
                    .count();
                if (initialCount > 1) {
                    throw new IllegalArgumentException("Only one INITIAL step is allowed");
                }
            }
            case TRANSFORM -> {
                // TRANSFORM steps must have INITIAL step before them
                if (position == 0) {
                    throw new IllegalArgumentException("TRANSFORM step cannot be at position 0");
                }
                // Check no INITIAL steps after this position
                for (int i = position + 1; i < stepTypesAfterChange.size(); i++) {
                    if (stepTypesAfterChange.get(i).getPosition() == StepPosition.INITIAL) {
                        throw new IllegalArgumentException("TRANSFORM step cannot have INITIAL steps after it");
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
