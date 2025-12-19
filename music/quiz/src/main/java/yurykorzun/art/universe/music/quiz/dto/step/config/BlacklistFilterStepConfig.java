package yurykorzun.art.universe.music.quiz.dto.step.config;

import yurykorzun.art.universe.music.quiz.entity.StepType;

import java.util.List;

public record BlacklistFilterStepConfig(
    List<Long> categoryIds
) implements StepConfig {

    @Override
    public StepType getType() {
        return StepType.BLACKLIST_FILTER;
    }
}
