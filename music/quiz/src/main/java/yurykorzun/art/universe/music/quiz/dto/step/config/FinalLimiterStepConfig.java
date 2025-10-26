package yurykorzun.art.universe.music.quiz.dto.step.config;

import yurykorzun.art.universe.music.quiz.entity.StepType;

public record FinalLimiterStepConfig(
    Integer targetCount
) implements StepConfig {

    @Override
    public StepType getType() {
        return StepType.FINAL_LIMITER;
    }
}
