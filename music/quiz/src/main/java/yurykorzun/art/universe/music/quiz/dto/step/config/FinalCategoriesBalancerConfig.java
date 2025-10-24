package yurykorzun.art.universe.music.quiz.dto.step.config;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import yurykorzun.art.universe.music.quiz.entity.StepType;

import java.util.List;

@Getter
@EqualsAndHashCode(callSuper = true)
public class FinalCategoriesBalancerConfig extends StepConfig {

    private final Integer targetCount;
    private final List<CategoryWeight> categories;
    private final Double defaultQuota;
    
    public FinalCategoriesBalancerConfig(Integer targetCount, List<CategoryWeight> categories, Double defaultQuota) {
        this.targetCount = targetCount;
        this.categories = categories;
        this.defaultQuota = defaultQuota;
    }

    @Override
    public StepType getType() {
        return StepType.FINAL_CATEGORIES_BALANCER;
    }
}
