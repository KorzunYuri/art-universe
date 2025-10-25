package yurykorzun.art.universe.music.quiz.dto.step.config;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import yurykorzun.art.universe.music.quiz.entity.StepType;

import java.util.List;

@Getter
@EqualsAndHashCode(callSuper = true)
public class FinalCategoriesBalancerConfig extends StepConfig {

    private final Integer targetCount;
    private final Double defaultQuota;
    private final List<CategoryWeight> categories;
    
    public FinalCategoriesBalancerConfig(Integer targetCount, Double defaultQuota, List<CategoryWeight> categories) {
        this.targetCount = targetCount;
        this.defaultQuota = defaultQuota;
        this.categories = categories;
    }

    @Override
    public StepType getType() {
        return StepType.FINAL_CATEGORIES_BALANCER;
    }
}
