package yurykorzun.art.universe.music.quiz.dto.step.config;

import yurykorzun.art.universe.music.quiz.entity.StepType;

import java.util.List;

public record FinalCategoriesBalancerConfig(
    Integer targetCount,
    Double defaultQuota,
    List<CategoryWeight> categories
) implements StepConfig {

    @Override
    public StepType getType() {
        return StepType.FINAL_CATEGORIES_BALANCER;
    }
}
