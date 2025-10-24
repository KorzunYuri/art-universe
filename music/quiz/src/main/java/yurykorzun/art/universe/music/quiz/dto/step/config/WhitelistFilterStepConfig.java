package yurykorzun.art.universe.music.quiz.dto.step.config;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import yurykorzun.art.universe.music.quiz.entity.StepType;

import java.util.List;

@Getter
@EqualsAndHashCode(callSuper = true)
public class WhitelistFilterStepConfig extends StepConfig {

    private final List<CategoryWeight> categories;
    
    public WhitelistFilterStepConfig(List<CategoryWeight> categories) {
        this.categories = categories;
    }

    @Override
    public StepType getType() {
        return StepType.WHITELIST_FILTER;
    }
}
