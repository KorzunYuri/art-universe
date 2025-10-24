package yurykorzun.art.universe.music.quiz.dto.step.config;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import yurykorzun.art.universe.music.quiz.entity.StepType;

@Getter
@EqualsAndHashCode(callSuper = true)
public class StartDatasourceStepConfig extends StepConfig {

    public final static String DEFAULT_DATASOURCE = "mu_view.v_track";

    @Override
    public StepType getType() {
        return StepType.START_DATASOURCE;
    }
}
