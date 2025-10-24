package yurykorzun.art.universe.music.quiz.dto.step.config;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import yurykorzun.art.universe.music.quiz.entity.StepType;

import java.util.List;

@Getter
@EqualsAndHashCode(callSuper = true)
public class BlacklistFilterStepConfig extends StepConfig {

    private final List<Long> categoryIds;
    
    public BlacklistFilterStepConfig(List<Long> categoryIds) {
        this.categoryIds = categoryIds;
    }

    @Override
    public StepType getType() {
        return StepType.BLACKLIST_FILTER;
    }
}
