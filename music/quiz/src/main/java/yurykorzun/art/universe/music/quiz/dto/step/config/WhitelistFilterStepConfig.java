package yurykorzun.art.universe.music.quiz.dto.step.config;

import yurykorzun.art.universe.music.quiz.entity.StepType;

import java.util.List;

public record WhitelistFilterStepConfig(
    List<CategoryWeight> categories
) implements StepConfig {

    @Override
    public StepType getType() {
        return StepType.WHITELIST_FILTER;
    }
}
