package yurykorzun.art.universe.music.quiz.entity.step;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import yurykorzun.art.universe.music.quiz.entity.GenerationStepType;

import java.util.List;

@Getter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class FinalCategoriesBalancerStep extends FinalGenerationStep {
    private final Integer targetCount;
    private final List<CategoryWeight> categories;
    private final Double defaultQuota;
    
    public FinalCategoriesBalancerStep(Integer targetCount, List<CategoryWeight> categories, Double defaultQuota) {
        super(GenerationStepType.FINAL_CATEGORIES_BALANCER);
        this.targetCount = targetCount;
        this.categories = categories;
        this.defaultQuota = defaultQuota;
    }
    
    public record CategoryWeight(Long id, Double weight) {}
}
