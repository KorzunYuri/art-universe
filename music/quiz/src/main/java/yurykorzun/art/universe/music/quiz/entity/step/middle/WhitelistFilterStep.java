package yurykorzun.art.universe.music.quiz.entity.step.middle;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import yurykorzun.art.universe.music.quiz.entity.step.GenerationStepType;

import java.util.List;

@Getter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class WhitelistFilterStep extends MiddleGenerationStep {
    private final List<CategoryWeight> categories;
    
    public WhitelistFilterStep(List<CategoryWeight> categories) {
        super(GenerationStepType.WHITELIST_FILTER);
        this.categories = categories;
    }
    
    public record CategoryWeight(Long id, Double weight) {}
}
