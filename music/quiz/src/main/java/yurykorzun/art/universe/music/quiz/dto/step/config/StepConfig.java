package yurykorzun.art.universe.music.quiz.dto.step.config;

import yurykorzun.art.universe.music.quiz.entity.StepType;

public sealed interface StepConfig
    permits StartDatasourceStepConfig,
            BlacklistFilterStepConfig,
            WhitelistFilterStepConfig,
    FinalLimiterStepConfig,
            FinalCategoriesBalancerConfig {
    StepType getType();
}
